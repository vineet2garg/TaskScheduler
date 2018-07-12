package com.springboot.microservice.taskscheduler.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.springboot.microservice.taskscheduler.model.TaskStatus;

@JsonSerialize
public class TaskResponse {

	private String appName = null;
	private String taskName = null;
	private TaskStatus taskStatus = null;
	private String startTime = null;
	private String lastSuccessStartTime = null;
	private String lastFailStartTime = null;
	private String message = null;

	public TaskResponse() {
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

	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(TaskStatus taskStatus) {
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskResponse [appId=")
				.append(appName)
				.append(", taskName=")
				.append(taskName)
				.append(", taskStatus=")
				.append(taskStatus)
				.append(", startTime=")
				.append(startTime)
				.append(", lastSuccessStartTime=")
				.append(lastSuccessStartTime)
				.append(", lastFailStartTime=")
				.append(lastFailStartTime)
				.append("]");
		return builder.toString();
	}

}
