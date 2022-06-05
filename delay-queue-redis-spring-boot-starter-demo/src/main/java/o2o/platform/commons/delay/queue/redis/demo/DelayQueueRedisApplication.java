package o2o.platform.commons.delay.queue.redis.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import o2o.platform.commons.delay.queue.redis.data.starter.EnabledDelayQueue;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@SpringBootApplication
@EnabledDelayQueue
public class DelayQueueRedisApplication {
    public static void main(String[] args) {
        SpringApplication.run(DelayQueueRedisApplication.class, args);
    }
}
