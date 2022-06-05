package o2o.platform.commons.delay.queue.redis.core.properties;

import java.time.Duration;

import lombok.Data;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@Data
public class ThreadPoolExecutorProperties {
    private int coreSize;
    private int maxSize;
    private int queueSize;
    private String threadNamePrefix;
    private Duration shutdownAwaitTime;

    public ThreadPoolExecutorProperties() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        this.coreSize = availableProcessors;
        this.maxSize = availableProcessors;
        this.queueSize = 5000;
        this.threadNamePrefix = "delay-queue-consumer-worker-";
        this.shutdownAwaitTime = Duration.ofSeconds(10);
    }
}
