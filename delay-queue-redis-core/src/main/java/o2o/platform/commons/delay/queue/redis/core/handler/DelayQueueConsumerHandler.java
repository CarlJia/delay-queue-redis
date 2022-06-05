package o2o.platform.commons.delay.queue.redis.core.handler;

import o2o.platform.commons.delay.queue.redis.core.message.DelayMessage;

/**
 * 延迟消息处理
 *
 * @author zhouyang01
 * Created on 2022-06-04
 */
public interface DelayQueueConsumerHandler {

    void onMessage(DelayMessage delayMessage);
}
