package com.springboot.microservice.taskscheduler.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.springboot.microservice.taskscheduler.model.TaskExecutionType;

@JsonSerialize
public class TaskRequest {
	private String appName = null;
	private String taskName = null;

	private TaskExecutionType executionType;

	private String fixedExecutionInterval;
	private String fixedExecutionUnit;

	private String cronExpression;

	private String endpoint = null;

	public TaskRequest() {
		// TODO Auto-generated constructor stub
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public TaskExecutionType getExecutionType() {
		return executionType;
	}

	public void setExecutionType(TaskExecutionType executionType) {
		this.executionType = executionType;
	}

	public String getFixedExecutionInterval() {
		return fixedExecutionInterval;
	}

	public void setFixedExecutionInterval(String fixedExecutionInterval) {
		this.fixedExecutionInterval = fixedExecutionInterval;
	}

	public String getFixedExecutionUnit() {
		return fixedExecutionUnit;
	}

	public void setFixedExecutionUnit(String fixedExecutionUnit) {
		this.fixedExecutionUnit = fixedExecutionUnit;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskRequest [appName=")
				.append(appName)
				.append(", taskName=")
				.append(taskName)
				.append(", executionType=")
				.append(executionType)
				.append(", fixedExecutionInterval=")
				.append(fixedExecutionInterval)
				.append(", fixedExecutionUnit=")
				.append(fixedExecutionUnit)
				.append(", cronExpression=")
				.append(cronExpression)
				.append(", endpoint=")
				.append(endpoint)
				.append("]");
		return builder.toString();
	}

}
