/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server;

import io.litmusblox.server.constant.IConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author : Shital Raval
 * Date : 3/2/20
 * Time : 10:40 AM
 * Class Name : AsyncConfiguration
 * Project Name : server
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean(name = "asyncTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        final Executor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        ((ThreadPoolTaskExecutor) threadPoolTaskExecutor).setCorePoolSize(IConstant.ASYNC_CORE_THREAD_POOL_SIZE);
        ((ThreadPoolTaskExecutor) threadPoolTaskExecutor).setMaxPoolSize(IConstant.ASYNC_MAX_THREAD_POOL_SIZE);
        ((ThreadPoolTaskExecutor) threadPoolTaskExecutor).setThreadNamePrefix("Async Thread - ");
        ((ThreadPoolTaskExecutor) threadPoolTaskExecutor).initialize();
        return threadPoolTaskExecutor;
    }
}
