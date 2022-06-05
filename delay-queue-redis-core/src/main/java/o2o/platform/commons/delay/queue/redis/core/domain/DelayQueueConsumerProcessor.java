package o2o.platform.commons.delay.queue.redis.core.domain;

import java.util.Map;

import lombok.Data;
import o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionHandler;
import o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerHandler;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@Data
public class DelayQueueConsumerProcessor {
    private Map<String, DelayQueueConsumerHandler> consumerHandlerMap;
    private Map<String, DelayQueueConsumerExceptionHandler> consumerExceptionHandlerMap;
}
