package io.yyy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时任务配置
 * Bean named 'defaultSockJsTaskScheduler' is expected to be of type 'org.springframework.scheduling.TaskScheduler' but was actually of type 'org.springframework.beans.factory.support.NullBean'
 * 警告大致意思是：web应用程序[ROOT]似乎启动了一个名为[ForkJoinPool.commonPool-worker-3]但是不能停止这个线程。这很可能会造成内存泄漏。
 * websocket需要创建线程，定时任务也需要创建线程，应该是websocket和定时任务在创建线程的时候发生了冲突，定时任务创建线程没有成功。
 * 这个时候我们手动创建一个定时任务的线程池，使得定时任务使用自己的线线程池，就能解决冲突。
 */
@Configuration
public class ScheduledConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduling = new ThreadPoolTaskScheduler();
        scheduling.setPoolSize(10);
        scheduling.initialize();
        return scheduling;
    }
}
