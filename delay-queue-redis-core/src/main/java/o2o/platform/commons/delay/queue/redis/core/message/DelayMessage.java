package o2o.platform.commons.delay.queue.redis.core.message;

import java.time.Duration;
import java.util.UUID;

import lombok.Data;

/**
 * @author zhouyang01
 * Created on 2022-06-04
 */
@Data
public class DelayMessage {
    /**
     * 消息ID
     */
    private String messageId;
    /**
     * 消息主题
     */
    private String topic;
    /**
     * 消息延迟，绝对毫秒
     */
    private long delay;
    /**
     * 消息创建时间，绝对毫秒
     */
    private long createTime;
    /**
     * 消息主体内容对象
     */
    private Object payload;

    /**
     * 已经重试的次数
     */
    private int alreadyRetryTimes;

    public DelayMessage() {
        this.messageId = UUID.randomUUID().toString();
        this.createTime = System.currentTimeMillis();
        this.alreadyRetryTimes = 0;
    }

    public DelayMessage(String topic, Duration delay, Object payload) {
        this();
        this.topic = topic;
        this.payload = payload;
        this.delay = delay.toMillis();
    }
}
