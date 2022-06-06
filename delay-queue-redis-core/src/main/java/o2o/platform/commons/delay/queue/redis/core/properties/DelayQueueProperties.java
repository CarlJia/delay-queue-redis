package o2o.platform.commons.delay.queue.redis.core.properties;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@Data
public class DelayQueueProperties {
    /**
     * 服务标记
     */
    private String appName;
    /**
     * 当前环境,非必填。形如:dev,pre,prod等。部分服务，多个环境使用一套redis，通过key前缀来标记
     */
    private String env;
    /**
     * 消息消费执行线程配置
     */
    private ThreadPoolExecutorProperties executor = new ThreadPoolExecutorProperties();
    /**
     * 生产者配置
     */
    private ProduceProperties p = new ProduceProperties();
    /**
     * 消费者配置
     */
    private ConsumeProperties c = new ConsumeProperties();
    /**
     * 主题相关配置
     */
    private Map<String, TopicProperties> topics = new HashMap<>();
}
