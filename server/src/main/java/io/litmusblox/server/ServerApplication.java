/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */
package io.litmusblox.server;

import io.litmusblox.server.constant.IConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Main application class
 *
 * @author : Shital Raval
 * Date : 26/6/19
 * Time : 11:51 AM
 * Class Name : ServerApplication
 * Project Name : server
 */
@EnableConfigurationProperties
@EnableScheduling
@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

	@Bean
	public TaskScheduler taskScheduler() {
		final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(IConstant.SCHEDULER_THREAD_POOL_SIZE);
		return scheduler;
	}

}
