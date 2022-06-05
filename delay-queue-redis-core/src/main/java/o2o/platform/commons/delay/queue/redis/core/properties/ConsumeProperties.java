package o2o.platform.commons.delay.queue.redis.core.properties;

import java.time.Duration;

import lombok.Data;

/**
 * 消费者属性对象
 *
 * @author zhouyang01
 * Created on 2022-06-04
 */
@Data
public class ConsumeProperties {

    /**
     * 是否开启
     */
    private boolean enabled;

    /**
     * 服务初始化时候的延迟启动时间
     */
    private Duration initialDelay;

    /**
     * 扫描间隔
     */
    private Duration scanInterval;

    /**
     * 服务关闭前等待的时间
     */
    private Duration shutdownAwaitTime;


    public ConsumeProperties() {
        this.enabled = true;
        this.initialDelay = Duration.ofSeconds(5);
        this.scanInterval = Duration.ofSeconds(1);
        this.shutdownAwaitTime = Duration.ofSeconds(10);
    }
}
