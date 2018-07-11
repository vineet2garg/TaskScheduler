package com.springboot.microservice.taskscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.springboot.microservice.taskscheduler.controller.TaskSchedulerController;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableJpaAuditing
@ComponentScan(basePackageClasses = { TaskSchedulerController.class })
public class TaskSchedulerApplication {
	private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerApplication.class);

	@Value("${api.description}")
	private String apiDescription = null;

	@Value("${api.title}")
	private String apiTitle = null;

	@Value("${api.version}")
	private String apiVersion = null;

	public static void main(String[] args) {
		logger.info("TaskScheduler Application Started!");
		SpringApplication.run(TaskSchedulerApplication.class, args);
	}

	@Bean
	public TaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadNamePrefix("Boral-TaskScheduler-");
		scheduler.setPoolSize(10);
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		scheduler.setAwaitTerminationSeconds(10);
		return scheduler;
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.any()).paths(paths()).build();
	}

	// Describe APIs
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(apiTitle).description(apiDescription).version(apiVersion).build();
	}

	// Only select APIs that matches the given Predicates.
	private Predicate<String> paths() {
		// Match all paths except /error
		return Predicates.and(PathSelectors.regex("/.*"), Predicates.not(PathSelectors.regex("/error.*")));
	}
}
