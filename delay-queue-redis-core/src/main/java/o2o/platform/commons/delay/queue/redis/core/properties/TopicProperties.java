package o2o.platform.commons.delay.queue.redis.core.properties;

import lombok.Data;

/**
 * @author zhouyang01
 * Created on 2022-06-04
 */
@Data
public class TopicProperties {
    /**
     * 是否开启
     */
    private boolean enabled;
    /**
     * 消费handler名称，默认是类的simpleName的驼峰
     */
    private String consumerHandlerName;
    /**
     * 消费异常handler名称，默认是类的simpleName的驼峰
     */
    private String consumerExceptionHandlerName;
    /**
     * 每次查询批次
     */
    private int fetchSize;
    /**
     * 消息失败最大重试次数
     */
    private int maxRetry;
    /**
     * 重试间隔
     */
    private long retryInterval;

    public TopicProperties() {
        this.enabled = true;
        this.consumerExceptionHandlerName = "delayQueueConsumerExceptionReentrantHandler";
        this.fetchSize = 10;
        this.maxRetry = 3;
        this.retryInterval = 1000;
    }
}
