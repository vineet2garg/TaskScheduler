package com.springboot.microservice.taskscheduler.controller;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

public class WorkerTask implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(WorkerTask.class);
	private String appId = null;
	private String taskId = null;
	private String startTime = null;
	private String endpoint = null;

	public WorkerTask(String appId, String taskId, String startTime, String endpoint) {
		this.appId = appId;
		this.taskId = taskId;
		this.startTime = startTime;
		this.endpoint = endpoint;
	}

	@Override
	public void run() {
		logger.info("Running Task: AppId: %s, TaskId: %s, started at %s with endpoint %s. Current Time : %s", this.appId, this.taskId, this.startTime,
				this.endpoint, Instant.now().toString());
		if (!StringUtils.isEmpty(this.endpoint)) {
			testConnectivity();
		}
	}

	private void testConnectivity() {
		ResponseEntity<Object> response = new RestTemplate().getForEntity(this.endpoint, Object.class);
		if (response.getStatusCode().is2xxSuccessful()) {
			logger.info("Successfully Called!!!! HTTP URL:" + this.endpoint);
		} else {
			logger.info("Failure Called!!!! HTTP URL:" + this.endpoint);
		}
	}
}
