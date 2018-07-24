package com.springboot.microservice.taskscheduler.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;

import com.springboot.microservice.taskscheduler.model.TaskExecutionType;

public class TaskExecutionTypeValidator implements ConstraintValidator<ValidExecutionType, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		// TODO Auto-generated method stub
		boolean valid = false;
		if (!StringUtils.isEmpty(value)) {
			for (TaskExecutionType executionType : TaskExecutionType.values()) {
				valid = executionType.name().equalsIgnoreCase(value);
				if (valid) {
					return true;
				}
			}
		}
		return false;
	}

}
