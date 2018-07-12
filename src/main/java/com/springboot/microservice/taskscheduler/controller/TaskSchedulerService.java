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

import com.springboot.microservice.taskscheduler.model.TaskExecutionType;
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

	@Autowired
	private TaskScheulerRepository taskSchedulerRepository;

	private ConcurrentHashMap<Long, ScheduledFuture<?>> taskInExecution = new ConcurrentHashMap<>();

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
		TaskSchedule taskScheduleId = upsertTaskInfoToDB(appId, taskId, TaskStatus.CREATED, request, true);
		TaskResponse response = new TaskResponse();
		response.setAppName(taskScheduleId.getAppName());
		response.setTaskName(taskScheduleId.getTaskName());
		response.setTaskStatus(TaskStatus.CREATED);
		response.setMessage("Task Created Successfully.");
		return response;
	}

	public TaskResponse deleteTaskForApplication(String appId, String taskId, TaskRequest request) {
		TaskSchedule taskSchedule = taskSchedulerRepository.findByAppIdAndTaskId(appId, taskId);
		TaskResponse response = new TaskResponse();
		if (null != taskSchedule) {
			taskSchedulerRepository.delete(taskSchedule);

			response.setAppName(taskSchedule.getAppName());
			response.setTaskName(taskSchedule.getTaskName());
			response.setTaskStatus(TaskStatus.DELETED);
			response.setMessage("Task Deleted Successfully.");
		} else {
			response.setAppName(appId);
			response.setTaskName(taskId);
			response.setTaskStatus(TaskStatus.FAILED);
			response.setMessage("No Task in DB.");

		}
		return response;
	}

	public TaskResponse startTaskExecution(String appId, String taskId) {
		TaskResponse response = new TaskResponse();

		// Updating Task Status in DB
		TaskSchedule taskSchedule = upsertTaskInfoToDB(appId, taskId, TaskStatus.STARTED, null, false);
		if (null != taskSchedule) {
			// Start the Poller
			scheduleTask(appId, taskId, taskSchedule);

			response.setAppName(taskSchedule.getAppName());
			response.setTaskName(taskSchedule.getTaskName());
			response.setTaskStatus(TaskStatus.STARTED);
			response.setStartTime(taskSchedule.getExecutionStartTime());
			response.setLastSuccessStartTime(taskSchedule.getLastSuccessExecutionStartTime());
			response.setMessage("Task Started Successfully.");
		} else {
			response.setAppName(appId);
			response.setTaskName(taskId);
			response.setTaskStatus(TaskStatus.FAILED);
			response.setMessage("No Task in DB.");
		}

		return response;
	}

	public TaskResponse stopTaskExecution(String appId, String taskId) {
		TaskResponse response = new TaskResponse();

		// Fetch the Task Details from DB
		TaskSchedule taskSchedule = upsertTaskInfoToDB(appId, taskId, TaskStatus.STOPPED, null, false);
		if (taskSchedule != null) {
			ScheduledFuture<?> scheduledFuture = taskInExecution.get(taskSchedule.getId());
			if (scheduledFuture != null && scheduledFuture.cancel(false)) {
				response.setStartTime(taskSchedule.getExecutionStartTime());
				response.setTaskStatus(TaskStatus.STOPPED);
				response.setMessage("Task Stopped Successfully.");

				// Remove the Scheduler from Cache
				taskInExecution.remove(taskSchedule.getId());
			} else {
				response.setTaskStatus(TaskStatus.FAILED);
				response.setMessage("Task Failed to Stop the task or task is not running.");
			}

			response.setLastSuccessStartTime(taskSchedule.getLastSuccessExecutionStartTime());
			response.setAppName(taskSchedule.getAppName());
			response.setTaskName(taskSchedule.getTaskName());
		} else {
			response.setAppName(appId);
			response.setTaskName(taskId);
			response.setTaskStatus(TaskStatus.FAILED);
			response.setMessage("No Task in DB.");
		}

		return response;
	}

	private void scheduleTask(String appId, String taskId, TaskSchedule taskSchedule) {
		ScheduledFuture<?> scheduledFuture = null;
		WorkerTask workerTask = new WorkerTask(appId, taskId, String.valueOf(Instant.now()), taskSchedule.getEndpoint());
		switch (TaskExecutionType.valueOf(taskSchedule.getExecutionType())) {
			case FIXED:
				// Fixed Periodic Trigger
				// import java.util.concurrent.TimeUnit;
				Trigger perodicTrigger = new PeriodicTrigger(Integer.valueOf(taskSchedule.getFixedExecutionInterval()),
						TimeUnit.valueOf(taskSchedule.getFixedExecutionUnit()));
				scheduledFuture = taskScheduler.schedule(workerTask, perodicTrigger);
				break;
			case CRON:
				// CRON Expression Trigger
				Trigger cronTrigger = new CronTrigger(taskSchedule.getCronExpression());
				scheduledFuture = taskScheduler.schedule(workerTask, cronTrigger);
				break;
			default:
				logger.error("Incorrect Execution Type : {}", taskSchedule.getExecutionType());
		}

		this.taskInExecution.putIfAbsent(taskSchedule.getId(), scheduledFuture);
	}

	private TaskSchedule upsertTaskInfoToDB(String appId, String taskId, TaskStatus taskStatus, TaskRequest request, boolean create) {
		// Check for previous task
		TaskSchedule taskSchedule = taskSchedulerRepository.findByAppIdAndTaskId(appId, taskId);
		if (null == taskSchedule && !create) {
			return taskSchedule;
		}

		if (null == taskSchedule) {
			// Adding to DB
			taskSchedule = new TaskSchedule();
			taskSchedule.setAppId(appId);
			taskSchedule.setAppName(request.getAppName());
			taskSchedule.setTaskId(taskId);
			taskSchedule.setTaskName(request.getTaskName());

			taskSchedule.setExecutionType(String.valueOf(request.getExecutionType()));
			switch (request.getExecutionType()) {
				case CRON:
					taskSchedule.setFixedExecutionInterval("");
					taskSchedule.setFixedExecutionUnit("");
					taskSchedule.setCronExpression(request.getCronExpression());
					break;
				case FIXED:
					taskSchedule.setFixedExecutionInterval(request.getFixedExecutionInterval());
					taskSchedule.setFixedExecutionUnit(request.getFixedExecutionUnit());
					taskSchedule.setCronExpression("");
					break;
				default:
					logger.error("Incorrect Execution Type : {}", request.getExecutionType());
			}

			taskSchedule.setEndpoint(request.getEndpoint());
		}

		switch (taskStatus) {
			case STARTED:
				taskSchedule.setExecutionStartTime(String.valueOf(Instant.now()));
				break;
			case STOPPED:
				taskSchedule.setLastSuccessExecutionStartTime(taskSchedule.getExecutionStartTime());
				break;
			default:
				logger.debug("No Action");
		}

		taskSchedule.setTaskStatus(taskStatus.name());
		taskSchedulerRepository.saveAndFlush(taskSchedule);
		logger.info("Task Saved : {}", taskSchedule);

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
