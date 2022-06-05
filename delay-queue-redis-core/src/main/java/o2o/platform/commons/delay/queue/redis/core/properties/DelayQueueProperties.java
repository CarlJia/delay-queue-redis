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
    private String appName;
    private ThreadPoolExecutorProperties executor = new ThreadPoolExecutorProperties();
    private ProduceProperties producer = new ProduceProperties();
    private ConsumeProperties consumer = new ConsumeProperties();
    private Map<String, TopicProperties> topics = new HashMap<>();
}
