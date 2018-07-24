package com.springboot.microservice.taskscheduler.exception;

import java.time.Instant;

public class ExceptionResponse {
	private Instant timestamp;
	private String errorCode;
	private String errorMessage;

	public ExceptionResponse(Instant timestamp, String errorCode, String errorMessage) {
		this.timestamp = timestamp;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

}