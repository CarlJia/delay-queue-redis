package o2o.platform.commons.delay.queue.redis.core.handler;

import o2o.platform.commons.delay.queue.redis.core.message.DelayMessage;

/**
 * 消息消费失败处理
 *
 * @author zhouyang01
 * Created on 2022-06-05
 */
public interface DelayQueueConsumerExceptionHandler {

    void onException(DelayMessage delayMessage, Throwable exception);
}
