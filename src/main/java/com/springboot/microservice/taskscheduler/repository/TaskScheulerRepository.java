package com.springboot.microservice.taskscheduler.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.springboot.microservice.taskscheduler.model.TaskSchedule;

@Repository
public interface TaskScheulerRepository extends JpaRepository<TaskSchedule, Long> {
	public List<TaskSchedule> findByAppId(String appId);

	public List<TaskSchedule> findByTaskId(String taskId);

	public List<TaskSchedule> findByTaskStatus(String taskStatus);

	@Query("SELECT ts FROM TaskSchedule ts WHERE ts.appId = :appId AND ts.taskId = :taskId")
	public Optional<TaskSchedule> findByAppIdAndTaskId(@Param("appId") String appId, @Param("taskId") String taskId);
}
