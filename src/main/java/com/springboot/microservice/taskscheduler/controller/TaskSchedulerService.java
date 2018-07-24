package com.springboot.microservice.taskscheduler.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
			response.add(generateTaskResponse(task, null));
		}
		logger.info("Fetching List of Applications : " + response);
		return response;
	}

	public List<TaskResponse> listTasksForApplication(String appId) {
		List<TaskResponse> response = new ArrayList<>();

		List<TaskSchedule> taskScheduled = taskSchedulerRepository.findByAppId(appId);
		for (TaskSchedule task : taskScheduled) {
			response.add(generateTaskResponse(task, null));
		}
		logger.info("Fetching List of Tasks for an Applications : " + response);
		return response;
	}

	public TaskResponse getTaskDetails(String appId, String taskId) {
		TaskSchedule taskSchedule = taskSchedulerRepository.findByAppIdAndTaskId(appId, taskId).orElse(null);

		TaskResponse response = new TaskResponse();
		if (null != taskSchedule) {
			response = generateTaskResponse(taskSchedule, null);
		} else {
			response.setAppId(appId);
			response.setTaskId(taskId);
			response.setTaskStatus(TaskStatus.FAILED.name());
			response.setMessage("No Task in DB.");
		}
		logger.info("Fetching details of a Task for an Applications : " + response);
		return response;
	}

	public TaskResponse createTaskForApplication(String appId, String taskId, TaskRequest request) {
		TaskSchedule taskSchedule = upsertTaskInfoToDB(appId, taskId, TaskStatus.CREATED, request, true);
		TaskResponse response = generateTaskResponse(taskSchedule, "Task Created Successfully.");
		return response;
	}

	public TaskResponse deleteTaskForApplication(String appId, String taskId, TaskRequest request) {
		TaskSchedule taskSchedule = taskSchedulerRepository.findByAppIdAndTaskId(appId, taskId).orElse(null);
		TaskResponse response = new TaskResponse();
		if (null != taskSchedule) {
			taskSchedulerRepository.delete(taskSchedule);

			response = generateTaskResponse(taskSchedule, "Task Deleted Successfully.");
			response.setTaskStatus(TaskStatus.DELETED.name());
		} else {
			response.setAppId(appId);
			response.setTaskId(taskId);
			response.setTaskStatus(TaskStatus.FAILED.name());
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
			if (scheduleTask(appId, taskId, taskSchedule)) {
				response = generateTaskResponse(taskSchedule, "Task Started Successfully.");
			} else {
				taskSchedule = upsertTaskInfoToDB(appId, taskId, TaskStatus.DELETED, null, false);
				response = generateTaskResponse(taskSchedule, "Invalid Task. It's Deleted.");
			}

		} else {
			response.setAppId(appId);
			response.setTaskId(taskId);
			response.setTaskStatus(TaskStatus.FAILED.name());
			response.setMessage("No Task in DB.");
		}

		return response;
	}

	public TaskResponse stopTaskExecution(String appId, String taskId) {
		TaskResponse response = new TaskResponse();

		// Fetch the Task Details from DB
		TaskSchedule taskSchedule = upsertTaskInfoToDB(appId, taskId, TaskStatus.STOPPED, null, false);
		if (taskSchedule != null) {
			// Remove the Scheduler from Cache
			ScheduledFuture<?> scheduledFuture = taskInExecution.remove(taskSchedule.getId());
			if (scheduledFuture != null && scheduledFuture.cancel(false)) {
				response = generateTaskResponse(taskSchedule, "Task Stopped Successfully.");
			} else {
				response = generateTaskResponse(taskSchedule, "Task is not running.");
			}
		} else {
			response.setAppId(appId);
			response.setTaskId(taskId);
			response.setTaskStatus(TaskStatus.FAILED.name());
			response.setMessage("No Task in DB.");
		}
		return response;
	}

	private boolean scheduleTask(String appId, String taskId, TaskSchedule taskSchedule) {
		boolean started = false;
		Trigger trigger = null;
		switch (TaskExecutionType.valueOf(taskSchedule.getExecutionType())) {
		case FIXED:
			// Fixed Periodic Trigger
			// import java.util.concurrent.TimeUnit;
			trigger = new PeriodicTrigger(Integer.valueOf(taskSchedule.getFixedExecutionInterval()),
					TimeUnit.valueOf(taskSchedule.getFixedExecutionUnit()));
			break;
		case CRON:
			// CRON Expression Trigger
			trigger = new CronTrigger(taskSchedule.getCronExpression());
			break;
		default:
			logger.error("Incorrect Execution Type : {}", taskSchedule.getExecutionType());
		}

		if (null != trigger) {
			WorkerTask workerTask = new WorkerTask(appId, taskId, String.valueOf(Instant.now()),
					taskSchedule.getEndpoint());
			this.taskInExecution.putIfAbsent(taskSchedule.getId(), taskScheduler.schedule(workerTask, trigger));

			started = true;
		}

		return started;
	}

	private TaskSchedule upsertTaskInfoToDB(String appId, String taskId, TaskStatus taskStatus, TaskRequest request,
			boolean create) {
		// Check for previous task
		TaskSchedule taskSchedule = taskSchedulerRepository.findByAppIdAndTaskId(appId, taskId)
				.orElse(create ? createTaskScheduleObj(appId, taskId, request) : null);
		if (null == taskSchedule) {
			return taskSchedule;
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
		taskSchedule = taskSchedulerRepository.saveAndFlush(taskSchedule);
		logger.info("Task Saved : {}", taskSchedule);

		return taskSchedule;
	}

	private TaskSchedule createTaskScheduleObj(String appId, String taskId, TaskRequest request) {
		TaskSchedule taskSchedule = new TaskSchedule();
		taskSchedule.setAppId(appId);
		taskSchedule.setAppName(request.getAppName());
		taskSchedule.setTaskId(taskId);
		taskSchedule.setTaskName(request.getTaskName());

		taskSchedule.setExecutionType(String.valueOf(request.getExecutionType()));
		taskSchedule.setEndpoint(request.getEndpoint());

		switch (TaskExecutionType.valueOf(request.getExecutionType())) {
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

		return taskSchedule;

	}

	private TaskResponse generateTaskResponse(TaskSchedule taskSchedule, String message) {
		TaskResponse taskResponse = new TaskResponse();
		taskResponse.setAppId(taskSchedule.getAppId());
		taskResponse.setAppName(taskSchedule.getAppName());
		taskResponse.setTaskId(taskSchedule.getTaskId());
		taskResponse.setTaskName(taskSchedule.getTaskName());
		taskResponse.setTaskStatus(taskSchedule.getTaskStatus());
		taskResponse.setStartTime(taskSchedule.getExecutionStartTime());
		taskResponse.setLastSuccessStartTime(taskSchedule.getLastSuccessExecutionStartTime());

		taskResponse.setCronExpression(Optional.of(taskSchedule.getCronExpression()).get());
		taskResponse.setExecutionType(taskSchedule.getExecutionType());
		taskResponse.setFixedExecutionInterval(taskSchedule.getFixedExecutionInterval());
		taskResponse.setFixedExecutionUnit(taskSchedule.getFixedExecutionUnit());

		taskResponse.setMessage(message);

		return taskResponse;
	}
}
