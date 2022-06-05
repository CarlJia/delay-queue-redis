package o2o.platform.commons.delay.queue.redis.core.handler;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;

import o2o.platform.commons.delay.queue.redis.core.constants.ResultStatus;
import o2o.platform.commons.delay.queue.redis.core.domain.SendResult;
import o2o.platform.commons.delay.queue.redis.core.message.DelayMessage;
import o2o.platform.commons.delay.queue.redis.core.properties.DelayQueueProperties;
import o2o.platform.commons.delay.queue.redis.core.properties.TopicProperties;
import o2o.platform.commons.delay.queue.redis.core.service.DelayMessageProducer;

/**
 * 简单的重试消费异常处理
 * <p>
 * 如果量级极小的情况下，可以使用。阻塞当前线程一直重试成功或者达到最大次数
 *
 * @author zhouyang01
 * Created on 2022-06-05
 */
public class DelayQueueConsumerExceptionRetryHandler implements DelayQueueConsumerExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DelayQueueConsumerExceptionRetryHandler.class);
    private final DelayMessageProducer delayMessageProducer;
    private final DelayQueueProperties delayQueueProperties;

    public DelayQueueConsumerExceptionRetryHandler(DelayMessageProducer delayMessageProducer,
            DelayQueueProperties delayQueueProperties) {
        this.delayMessageProducer = delayMessageProducer;
        this.delayQueueProperties = delayQueueProperties;
    }

    @Override
    public void onException(DelayMessage delayMessage, Throwable exception) {
        TopicProperties topicProperties =
                Objects.requireNonNull(delayQueueProperties.getTopics()).get(delayMessage.getTopic());
        int alreadyRetryTimes = delayMessage.getAlreadyRetryTimes();
        long retryInterval = topicProperties.getRetryInterval();
        int maxRetry = topicProperties.getMaxRetry();
        boolean retryResult = false;
        while (alreadyRetryTimes < maxRetry && !retryResult) {
            try {
                delayMessage.setAlreadyRetryTimes(++alreadyRetryTimes);
                delayMessage.setDelay(Duration.ofMillis(10L).toMillis()); //设置10毫秒，避免消息发送拦截
                SendResult sendResult = delayMessageProducer.send(delayMessage);
                logger.info("message retry result, sendResult={}, message={}", sendResult, delayMessage);
                retryResult = sendResult.getResultStatus() == ResultStatus.SEND_SUCCESS;
            } catch (Exception e) {
                logger.error("重试消费失败 message={}", delayMessage);
                Uninterruptibles.sleepUninterruptibly(retryInterval, TimeUnit.MILLISECONDS);
            }
        }
    }
}
