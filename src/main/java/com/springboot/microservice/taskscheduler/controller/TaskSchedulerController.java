package com.springboot.microservice.taskscheduler.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.microservice.taskscheduler.model.TaskStatus;
import com.springboot.microservice.taskscheduler.vo.TaskRequest;
import com.springboot.microservice.taskscheduler.vo.TaskResponse;

@RestController
@RequestMapping(value = "/taskScheduler")
public class TaskSchedulerController {
	private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerController.class);

	@Autowired
	private TaskSchedulerService taskSchedulerService;

	@GetMapping(value = "/")
	public @ResponseBody ResponseEntity<List<TaskResponse>> getApplications() {
		logger.info("List Applications :Request: ");
		List<TaskResponse> taskResponse = taskSchedulerService.listApplications();
		logger.info("List Applications :Response: {}", taskResponse);
		return ResponseEntity.ok(taskResponse);
	}

	@GetMapping(value = "/{appId}")
	public @ResponseBody ResponseEntity<List<TaskResponse>> getTasksOfApplication(@PathVariable String appId) {
		logger.info("List Tasks for an Applications :Request: {} ", appId);
		List<TaskResponse> taskResponse = taskSchedulerService.listTasksForApplication(appId);
		logger.info("List Tasks for an Applications :Response: {} ", taskResponse);
		return ResponseEntity.ok(taskResponse);
	}

	@GetMapping(value = "/{appId}/{taskId}")
	public @ResponseBody ResponseEntity<TaskResponse> getTaskOfApplication(@PathVariable String appId,
			@PathVariable String taskId) {
		logger.info("List Task of an Applications :Request: {}, {} ", appId, taskId);
		TaskResponse taskResponse = taskSchedulerService.getTaskDetails(appId, taskId);
		logger.info("List Task of an Applications :Response: {} ", taskResponse);
		return ResponseEntity.ok(taskResponse);
	}

	@PostMapping(value = "/{appId}/{taskId}")
	public @ResponseBody ResponseEntity<TaskResponse> createTaskOfApplication(@PathVariable String appId,
			@PathVariable String taskId, @Valid @RequestBody TaskRequest request) {
		logger.info("Create Task of an Applications :Request: {}, {} ", appId, taskId);
		TaskResponse taskResponse = taskSchedulerService.createTaskForApplication(appId, taskId, request);
		logger.info("Create Task of an Applications :Response: {} ", taskResponse);
		return ResponseEntity.status(HttpStatus.CREATED).body(taskResponse);
	}

	@DeleteMapping(value = "/{appId}/{taskId}")
	public @ResponseBody ResponseEntity<TaskResponse> deleteTaskOfApplication(@PathVariable String appId,
			@PathVariable String taskId, @Valid @RequestBody TaskRequest request) {
		logger.info("Delete Task of an Applications :Request: {}, {} ", appId, taskId);
		TaskResponse taskResponse = taskSchedulerService.deleteTaskForApplication(appId, taskId, request);
		logger.info("Delete Task of an Applications :Response: {} ", taskResponse);
		return ResponseEntity.ok(taskResponse);
	}

	@GetMapping(value = "/{appId}/{taskId}/start")
	public @ResponseBody ResponseEntity<TaskResponse> startExecution(@PathVariable String appId,
			@PathVariable String taskId) {
		logger.info("Starting the Task :Request: {}, {}", appId, taskId);
		TaskResponse taskResponse = taskSchedulerService.startTaskExecution(appId, taskId);
		logger.info("Starting the Task :Response: {}, {}", appId, taskId);
		return (TaskStatus.STARTED.name().equalsIgnoreCase(taskResponse.getTaskStatus()))
				? ResponseEntity.ok(taskResponse)
				: ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(taskResponse);
	}

	@GetMapping(value = "/{appId}/{taskId}/stop")
	public ResponseEntity<TaskResponse> stopExecution(@PathVariable String appId, @PathVariable String taskId) {
		logger.info("Stopping the Task :Request: {} , {}", appId, taskId);
		TaskResponse taskResponse = taskSchedulerService.stopTaskExecution(appId, taskId);
		logger.info("Stopping the Task :Response: {} , {}", appId, taskId);
		return (TaskStatus.STOPPED.name().equalsIgnoreCase(taskResponse.getTaskStatus()))
				? ResponseEntity.ok(taskResponse)
				: ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(taskResponse);
	}
}
