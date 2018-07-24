package com.springboot.microservice.taskscheduler.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "task_schedule")
public class TaskSchedule {

	@Id
	@GeneratedValue(generator = "task_schedule_generator")
	@SequenceGenerator(name = "task_schedule_generator", sequenceName = "task_schedule_sequence", initialValue = 1000)
	private Long id;

	@NotNull
	@Size(min = 3, max = 100)
	private String appId;

	@NotNull
	@Size(min = 3, max = 100)
	private String appName;

	@NotNull
	@Size(min = 3, max = 100)
	private String taskId;

	@NotNull
	@Size(min = 3, max = 100)
	private String taskName;

	@NotNull
	@Size(min = 3, max = 100)
	private String taskStatus;

	@NotNull
	@Size(min = 3, max = 100)
	private String executionType;

	private String fixedExecutionInterval;
	private String fixedExecutionUnit;

	private String cronExpression;

	private String executionStartTime;
	private String lastSuccessExecutionStartTime;
	private String lastFailExecutionStartTime;

	private String endpoint;

	public TaskSchedule() {
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
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

	public String getExecutionStartTime() {
		return executionStartTime;
	}

	public void setExecutionStartTime(String executionStartTime) {
		this.executionStartTime = executionStartTime;
	}

	public String getLastSuccessExecutionStartTime() {
		return lastSuccessExecutionStartTime;
	}

	public void setLastSuccessExecutionStartTime(String lastSuccessExecutionStartTime) {
		this.lastSuccessExecutionStartTime = lastSuccessExecutionStartTime;
	}

	public String getLastFailExecutionStartTime() {
		return lastFailExecutionStartTime;
	}

	public void setLastFailExecutionStartTime(String lastFailExecutionStartTime) {
		this.lastFailExecutionStartTime = lastFailExecutionStartTime;
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
		builder.append("ApplicationPollTask [appId=").append(appId).append(", appName=").append(appName)
				.append(", taskId=").append(taskId).append(", taskName=").append(taskName).append(", taskStatus=")
				.append(taskStatus).append(", executionType=").append(executionType).append(", fixedExecutionInterval=")
				.append(fixedExecutionInterval).append(", fixedExecutionUnit=").append(fixedExecutionUnit)
				.append(", cronExpression=").append(cronExpression).append(", executionStartTime=")
				.append(executionStartTime).append(", lastSuccessExecutionStartTime=")
				.append(lastSuccessExecutionStartTime).append(", lastFailExecutionStartTime=")
				.append(lastFailExecutionStartTime).append("]");
		return builder.toString();
	}

}
