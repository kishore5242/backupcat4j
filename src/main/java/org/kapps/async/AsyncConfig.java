package org.kapps.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
//    @Bean(name = "asyncExecutor")
//    public ThreadPoolTaskExecutor asyncExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(4);       // number of parallel threads
//        executor.setMaxPoolSize(8);        // max threads allowed
//        executor.setQueueCapacity(50);     // tasks waiting queue size
//        executor.setThreadNamePrefix("asyncExecutor-");
//        executor.initialize();
//        return executor;
//    }
}