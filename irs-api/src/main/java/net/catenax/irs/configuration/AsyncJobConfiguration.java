//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.configuration;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Executor
 */
@Configuration
@EnableAsync(proxyTargetClass = true)
@Profile("async")
public class AsyncJobConfiguration {

    @Value("${async.joborchestrator.poolSize}")
    Integer poolSize;

    @Value("${async.joborchestrator.maxPoolSize}")
    Integer maxPoolSize;

    @Value("${async.joborchestrator.capacity}")
    Integer capacity;

    @Bean(name = "asyncJobExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(capacity);
        executor.setThreadNamePrefix("AsynchJobThread-");
        executor.initialize();
        return executor;
    }

}
