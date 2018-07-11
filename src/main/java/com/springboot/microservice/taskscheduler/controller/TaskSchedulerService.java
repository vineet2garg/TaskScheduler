package com.springboot.microservice.taskscheduler.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import com.springboot.microservice.taskscheduler.model.TaskSchedule;
import com.springboot.microservice.taskscheduler.model.TaskStatus;
import com.springboot.microservice.taskscheduler.repository.TaskScheulerRepository;
import com.springboot.microservice.taskscheduler.vo.TaskRequest;
import com.springboot.microservice.taskscheduler.vo.TaskResponse;

@Service
public class TaskSchedulerService {
	private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerService.class);

	@Autowired
	private TaskScheduler taskScheduler;

	private ConcurrentHashMap<Long, ScheduledFuture<?>> taskInExecution = new ConcurrentHashMap<>();

	@Autowired
	private TaskScheulerRepository taskSchedulerRepository;

	public List<TaskResponse> listApplications() {
		List<TaskResponse> response = new ArrayList<>();

		List<TaskSchedule> taskScheduled = taskSchedulerRepository.findAll();
		for (TaskSchedule task : taskScheduled) {
			response.add(generateTaskResponse(task));
		}
		logger.info("Fetching List of Applications : " + response);
		return response;
	}

	public List<TaskResponse> listTasksForApplication(String appId) {
		List<TaskResponse> response = new ArrayList<>();

		List<TaskSchedule> taskScheduled = taskSchedulerRepository.findByAppId(appId);
		for (TaskSchedule task : taskScheduled) {
			response.add(generateTaskResponse(task));
		}
		logger.info("Fetching List of Tasks for an Applications : " + response);
		return response;
	}

	public TaskResponse getTaskDetails(String appId, String taskId) {
		TaskSchedule taskSchedule = taskSchedulerRepository.findByAppIdAndTaskId(appId, taskId);
		TaskResponse response = generateTaskResponse(taskSchedule);
		logger.info("Fetching details of a Task for an Applications : " + response);
		return response;
	}

	public TaskResponse createTaskForApplication(String appId, String taskId, TaskRequest request) {
		TaskSchedule taskScheduleId = saveTaskInfoToDB(appId, taskId, TaskStatus.CREATED, request, true);
		TaskResponse response = new TaskResponse();
		response.setAppName(taskScheduleId.getAppName());
		response.setTaskName(taskScheduleId.getTaskName());
		response.setTaskStatus(TaskStatus.CREATED);
		return response;
	}

	public TaskResponse deleteTaskForApplication(String appId, String taskId, TaskRequest request) {
		TaskSchedule taskScheduleId = saveTaskInfoToDB(appId, taskId, TaskStatus.DELETED, request, false);
		TaskResponse response = new TaskResponse();
		response.setAppName(taskScheduleId.getAppName());
		response.setTaskName(taskScheduleId.getTaskName());
		response.setTaskStatus(TaskStatus.DELETED);
		return response;
	}

	public TaskResponse startTaskExecution(String appId, String taskId, TaskRequest request) {
		ScheduledFuture<?> scheduledFuture = null;
		WorkerTask workerTask = new WorkerTask(appId, taskId, Instant.now().toString(), request.getEndpoint());
		switch (request.getExecutionType().toUpperCase()) {
		case "FIXED":
			// Fixed Periodic Trigger
			// import java.util.concurrent.TimeUnit;
			Trigger perodicTrigger = new PeriodicTrigger(Integer.valueOf(request.getFixedExecutionInterval()),
					TimeUnit.valueOf(request.getFixedExecutionUnit()));
			scheduledFuture = taskScheduler.schedule(workerTask, perodicTrigger);
			break;
		case "CRON":
			// CRON Expression Trigger
			Trigger cronTrigger = new CronTrigger(request.getCronExpression());
			scheduledFuture = taskScheduler.schedule(workerTask, cronTrigger);
			break;
		default:
			logger.error("Incorrect Execution Type : %s", request.getExecutionType());
		}

		TaskResponse response = new TaskResponse();

		// Adding to DB
		TaskSchedule taskSchedule = saveTaskInfoToDB(appId, taskId, TaskStatus.STARTED, request, false);
		if (null != taskSchedule) {
			taskInExecution.putIfAbsent(taskSchedule.getId(), scheduledFuture);

			response.setAppName(taskSchedule.getAppName());
			response.setTaskName(taskSchedule.getTaskName());
			response.setTaskStatus(TaskStatus.STARTED);
			response.setLastSuccessStartTime(taskSchedule.getLastSuccessExecutionStartTime());
			response.setStartTime(taskSchedule.getExecutionStartTime());
		} else {
			response.setAppName(request.getAppName());
			response.setTaskName(request.getTaskName());
			response.setTaskStatus(TaskStatus.FAILED);
			response.setFailureReason("No Task available to start.");
		}

		return response;
	}

	public TaskResponse stopTaskExecution(String appId, String taskId, TaskRequest request) {
		TaskResponse response = new TaskResponse();
		TaskSchedule taskSchedule = taskSchedulerRepository.findByAppIdAndTaskId(appId, taskId);

		ScheduledFuture<?> scheduledFuture = null;
		if (taskSchedule != null) {
			scheduledFuture = taskInExecution.get(taskSchedule.getId());
			response.setLastSuccessStartTime(taskSchedule.getExecutionStartTime());
			response.setAppName(taskSchedule.getAppName());
			response.setTaskName(taskSchedule.getTaskName());

			if (scheduledFuture != null && scheduledFuture.cancel(false)) {
				response.setStartTime(taskSchedule.getExecutionStartTime());
				response.setTaskStatus(TaskStatus.STOPPED);
				taskSchedule.setLastSuccessExecutionStartTime(taskSchedule.getExecutionStartTime());
				taskSchedule.setTaskStatus(TaskStatus.STOPPED.name());
				taskSchedulerRepository.saveAndFlush(taskSchedule);

				taskInExecution.remove(taskSchedule.getId());
			} else {
				response.setAppName(appId);
				response.setTaskName(taskId);
				response.setTaskStatus(TaskStatus.FAILED);
				response.setFailureReason("Task Failed to Start.");
			}
		} else {
			response.setAppName(appId);
			response.setTaskName(taskId);
			response.setTaskStatus(TaskStatus.FAILED);
			response.setFailureReason("Task Failed to Start.");
		}

		return response;
	}

	private TaskSchedule saveTaskInfoToDB(String appId, String taskId, TaskStatus taskStatus, TaskRequest request, boolean create) {
		// Check for previous task
		TaskSchedule taskSchedule = taskSchedulerRepository.findByAppIdAndTaskId(appId, taskId);

		if (null == taskSchedule && !create) {
			return null;
		}

		if (null == taskSchedule) {
			// Adding to DB
			taskSchedule = new TaskSchedule();
			taskSchedule.setAppId(appId);
			taskSchedule.setAppName(request.getAppName());
			taskSchedule.setTaskId(taskId);
			taskSchedule.setTaskName(request.getTaskName());
		} else {
			taskSchedule.setLastSuccessExecutionStartTime(taskSchedule.getExecutionStartTime());
		}

		taskSchedule.setExecutionType(request.getExecutionType());
		if ("FIXED".contentEquals(request.getExecutionType())) {
			taskSchedule.setFixedExecutionInterval(request.getFixedExecutionInterval());
			taskSchedule.setFixedExecutionUnit(request.getFixedExecutionUnit());
			taskSchedule.setCronExpression("");
		} else {
			taskSchedule.setFixedExecutionInterval("");
			taskSchedule.setFixedExecutionUnit("");
			taskSchedule.setCronExpression(request.getCronExpression());
		}

		taskSchedule.setTaskStatus(taskStatus.name());
		taskSchedule.setExecutionStartTime(Instant.now().toString());
		taskSchedule.setEndpoint(request.getEndpoint());

		taskSchedulerRepository.saveAndFlush(taskSchedule);

		logger.info("Task Saved : %s", taskSchedule);

		return taskSchedule;

	}

	private TaskResponse generateTaskResponse(TaskSchedule taskSchedule) {
		TaskResponse taskResponse = new TaskResponse();
		taskResponse.setAppName(taskSchedule.getAppName());
		taskResponse.setTaskName(taskSchedule.getTaskName());
		taskResponse.setTaskStatus(TaskStatus.valueOf(taskSchedule.getTaskStatus()));
		taskResponse.setStartTime(taskSchedule.getExecutionStartTime());
		taskResponse.setLastSuccessStartTime(taskSchedule.getLastSuccessExecutionStartTime());
		return taskResponse;
	}
}
