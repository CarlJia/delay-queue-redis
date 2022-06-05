package o2o.platform.commons.delay.queue.redis.core.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import o2o.platform.commons.delay.queue.redis.core.domain.SendResult;
import o2o.platform.commons.delay.queue.redis.core.message.DelayMessage;
import o2o.platform.commons.delay.queue.redis.core.properties.DelayQueueProperties;
import o2o.platform.commons.delay.queue.redis.core.properties.TopicProperties;
import o2o.platform.commons.delay.queue.redis.core.service.DelayMessageProducer;

/**
 * 可重入消费异常处理
 * <p>
 * 实现方式为：消息二次投递
 *
 * @author zhouyang01
 * Created on 2022-06-05
 */
public class DelayQueueConsumerExceptionReentrantHandler implements DelayQueueConsumerExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DelayQueueConsumerExceptionReentrantHandler.class);
    private final DelayMessageProducer delayMessageProducer;
    private final DelayQueueProperties delayQueueProperties;

    public DelayQueueConsumerExceptionReentrantHandler(DelayMessageProducer delayMessageProducer,
            DelayQueueProperties delayQueueProperties) {
        this.delayMessageProducer = delayMessageProducer;
        this.delayQueueProperties = delayQueueProperties;
    }

    @Override
    public void onException(DelayMessage delayMessage, Throwable exception) {
        TopicProperties topicProperties = delayQueueProperties.getTopics().get(delayMessage.getTopic());
        int maxRetry = topicProperties.getMaxRetry();
        long retryInterval = topicProperties.getRetryInterval();
        int alreadyRetryTimes = delayMessage.getAlreadyRetryTimes();
        if (alreadyRetryTimes < maxRetry) {
            delayMessage.setAlreadyRetryTimes(++alreadyRetryTimes);
            delayMessage.setDelay(retryInterval);
            SendResult sendResult = delayMessageProducer.send(delayMessage);
            logger.info("message retry result, sendResult={}, message={}", sendResult, delayMessage);
        } else {
            //TODO 报警
            logger.error("message consumer error, retry reach max times, message={}", delayMessage);
        }
    }
}
