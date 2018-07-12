package com.springboot.microservice.taskscheduler.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springboot.microservice.taskscheduler.model.TaskStatus;
import com.springboot.microservice.taskscheduler.vo.TaskRequest;
import com.springboot.microservice.taskscheduler.vo.TaskResponse;

@Controller
@RequestMapping(value = "/taskScheduler")
public class TaskSchedulerController {
	private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerController.class);

	@Autowired
	private TaskSchedulerService taskSchedulerService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<List<TaskResponse>> getApplications() {
		logger.info("List Applications :Request: ");
		List<TaskResponse> taskResponse = taskSchedulerService.listApplications();
		logger.info("List Applications :Response: {}", taskResponse);
		return new ResponseEntity<List<TaskResponse>>(taskResponse, HttpStatus.OK);
	}

	@RequestMapping(value = "/{appId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<List<TaskResponse>> getTasksOfApplication(@PathVariable String appId) {
		logger.info("List Tasks for an Applications :Request: {} ", appId);
		List<TaskResponse> taskResponse = taskSchedulerService.listTasksForApplication(appId);
		logger.info("List Tasks for an Applications :Response: {} ", taskResponse);
		return new ResponseEntity<List<TaskResponse>>(taskResponse, HttpStatus.OK);
	}

	@RequestMapping(value = "/{appId}/{taskId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<TaskResponse> getTaskOfApplication(@PathVariable String appId, @PathVariable String taskId) {
		logger.info("List Task of an Applications :Request: {}, {} ", appId, taskId);
		TaskResponse taskResponse = taskSchedulerService.getTaskDetails(appId, taskId);
		logger.info("List Task of an Applications :Response: {} ", taskResponse);
		return new ResponseEntity<TaskResponse>(taskResponse, HttpStatus.OK);
	}

	@RequestMapping(value = "/{appId}/{taskId}", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<TaskResponse> createTaskOfApplication(@PathVariable String appId, @PathVariable String taskId,
			@RequestBody TaskRequest request) {
		logger.info("Create Task of an Applications :Request: {}, {} ", appId, taskId);
		TaskResponse taskResponse = taskSchedulerService.createTaskForApplication(appId, taskId, request);
		logger.info("Create Task of an Applications :Response: {} ", taskResponse);
		return new ResponseEntity<TaskResponse>(taskResponse, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/{appId}/{taskId}", method = RequestMethod.DELETE)
	public @ResponseBody ResponseEntity<TaskResponse> deleteTaskOfApplication(@PathVariable String appId, @PathVariable String taskId,
			@RequestBody TaskRequest request) {
		logger.info("Delete Task of an Applications :Request: {}, {} ", appId, taskId);
		TaskResponse taskResponse = taskSchedulerService.deleteTaskForApplication(appId, taskId, request);
		logger.info("Delete Task of an Applications :Response: {} ", taskResponse);
		return new ResponseEntity<TaskResponse>(taskResponse, HttpStatus.OK);
	}

	@RequestMapping(value = "/{appId}/{taskId}/start", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<TaskResponse> startExecution(@PathVariable String appId, @PathVariable String taskId) {
		logger.info("Starting the Task :Request: {}, {}", appId, taskId);
		TaskResponse taskResponse = taskSchedulerService.startTaskExecution(appId, taskId);
		logger.info("Starting the Task :Response: {}, {}", appId, taskId);
		return (taskResponse.getTaskStatus() == TaskStatus.STARTED) ? new ResponseEntity<TaskResponse>(taskResponse, HttpStatus.OK)
				: new ResponseEntity<TaskResponse>(taskResponse, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@RequestMapping(value = "/{appId}/{taskId}/stop", method = RequestMethod.GET)
	public ResponseEntity<TaskResponse> stopExecution(@PathVariable String appId, @PathVariable String taskId) {
		logger.info("Stopping the Task :Request: {} , {}", appId, taskId);
		TaskResponse taskResponse = taskSchedulerService.stopTaskExecution(appId, taskId);
		logger.info("Stopping the Task :Response: {} , {}", appId, taskId);
		return (taskResponse.getTaskStatus() == TaskStatus.STOPPED) ? new ResponseEntity<TaskResponse>(taskResponse, HttpStatus.OK)
				: new ResponseEntity<TaskResponse>(taskResponse, HttpStatus.SERVICE_UNAVAILABLE);
	}
}
