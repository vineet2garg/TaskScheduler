package com.springboot.microservice.taskscheduler.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class TaskResponse {

	private String appId = null;
	private String appName = null;
	private String taskId = null;
	private String taskName = null;
	private String taskStatus = null;
	private String startTime = null;
	private String lastSuccessStartTime = null;
	private String lastFailStartTime = null;
	private String message = null;

	private String executionType;
	private String fixedExecutionInterval;
	private String fixedExecutionUnit;
	private String cronExpression;

	public TaskResponse() {
		// TODO Auto-generated constructor stub
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getLastSuccessStartTime() {
		return lastSuccessStartTime;
	}

	public void setLastSuccessStartTime(String lastSuccessStartTime) {
		this.lastSuccessStartTime = lastSuccessStartTime;
	}

	public String getLastFailStartTime() {
		return lastFailStartTime;
	}

	public void setLastFailStartTime(String lastFailStartTime) {
		this.lastFailStartTime = lastFailStartTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getExecutionType() {
		return executionType;
	}

	public void setExecutionType(String executionType) {
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskResponse [appId=");
		builder.append(appId);
		builder.append(", appName=");
		builder.append(appName);
		builder.append(", taskId=");
		builder.append(taskId);
		builder.append(", taskName=");
		builder.append(taskName);
		builder.append(", taskStatus=");
		builder.append(taskStatus);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append(", lastSuccessStartTime=");
		builder.append(lastSuccessStartTime);
		builder.append(", lastFailStartTime=");
		builder.append(lastFailStartTime);
		builder.append(", message=");
		builder.append(message);
		builder.append(", executionType=");
		builder.append(executionType);
		builder.append(", fixedExecutionInterval=");
		builder.append(fixedExecutionInterval);
		builder.append(", fixedExecutionUnit=");
		builder.append(fixedExecutionUnit);
		builder.append(", cronExpression=");
		builder.append(cronExpression);
		builder.append("]");
		return builder.toString();
	}

}
