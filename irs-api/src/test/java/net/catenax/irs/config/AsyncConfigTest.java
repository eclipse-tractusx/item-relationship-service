package net.catenax.irs.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/* @TestConfiguration
@EnableAsync(proxyTargetClass = true)
@Profile("asyncTest")*/
public class AsyncConfigTest {
    @Value("${async.joborchestrator.poolSize}")
    Integer poolSize;

    @Value("${async.joborchestrator.maxPoolSize}")
    Integer maxPoolSize;

    @Value("${async.joborchestrator.capacity}")
    Integer capacity;

    @Bean(name = "asyncJobExecutorTest")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(capacity);
        executor.setThreadNamePrefix("AsynchJobThreadTest-");
        executor.initialize();
        return executor;
    }
}
