package o2o.platform.commons.delay.queue.redis.core.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static o2o.platform.commons.delay.queue.redis.core.redis.RedisLuaUtils.getPersistentLuaContent;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import o2o.platform.commons.delay.queue.redis.core.constants.ResultStatus;
import o2o.platform.commons.delay.queue.redis.core.domain.SendResult;
import o2o.platform.commons.delay.queue.redis.core.exception.DelayQueueSendException;
import o2o.platform.commons.delay.queue.redis.core.message.DelayMessage;
import o2o.platform.commons.delay.queue.redis.core.properties.DelayQueueProperties;
import o2o.platform.commons.delay.queue.redis.core.redis.RedisKeyResolver;
import o2o.platform.commons.delay.queue.redis.core.redis.RedisOpService;
import o2o.platform.commons.delay.queue.redis.core.util.Jsons;

/**
 * 延迟消息生产
 *
 * @author zhouyang01
 * Created on 2022-06-04
 */
public class DelayMessageProducer {
    private static final Logger logger = LoggerFactory.getLogger(DelayMessageProducer.class);
    private final RedisOpService redisOpService;
    private final RedisKeyResolver redisKeyResolver;
    private final DelayQueueProperties delayQueueProperties;

    public DelayMessageProducer(RedisOpService redisOpService, RedisKeyResolver redisKeyResolver,
            DelayQueueProperties delayQueueProperties) {
        this.redisOpService = redisOpService;
        this.redisKeyResolver = redisKeyResolver;
        this.delayQueueProperties = delayQueueProperties;
    }

    public SendResult send(DelayMessage delayMessage) {
        if (!delayQueueProperties.getProducer().isEnabled()) {
            return SendResult.failure(ResultStatus.SEND_ENABLE_CLOSED, "发送消息开关为关闭状态");
        }
        logger.info("send delay message, {}", delayMessage);
        try {
            checkDelayMessage(delayMessage);
            sendDelayMessage(delayMessage);
        } catch (IllegalArgumentException e) {
            return SendResult.failure(ResultStatus.SEND_INVALID_PARAMETERS, e.getMessage());
        } catch (Exception e) {
            throw new DelayQueueSendException(
                    String.format("delay queue message exception, appName=%s, topic=%s, message=%s",
                            redisKeyResolver.getAppName(), delayMessage.getTopic(), delayMessage), e);
        }
        return SendResult.success(delayMessage.getMessageId());
    }


    private void checkDelayMessage(DelayMessage delayMessage) {
        checkArgument(delayMessage != null, "延迟消息对象为空");
        checkArgument(!isNullOrEmpty(delayMessage.getTopic()), "延迟消息Topic为空");
        checkArgument(delayMessage.getDelay() > 0 && Duration.ofMillis(delayMessage.getDelay()).toDays() <= 30,
                "延迟消息delay必须 >0 && <30days");
        checkArgument(delayMessage.getPayload() != null, "延迟消息内容不能为空");
    }

    private void sendDelayMessage(DelayMessage delayMessage) {
        List<String> keys = Lists.newArrayListWithCapacity(3);
        keys.add(redisKeyResolver.hashKey(delayMessage.getTopic()));
        keys.add(redisKeyResolver.key(delayMessage.getTopic(), delayMessage.getMessageId()));
        keys.add(redisKeyResolver.bucketKey(delayMessage.getTopic()));

        List<String> args = Lists.newArrayListWithCapacity(3);
        args.add(Jsons.toJson(delayMessage));
        args.add(String.valueOf(genDelayMessageScore(delayMessage.getDelay())));
        args.add(delayMessage.getMessageId());
        checkState(redisOpService.persist(getPersistentLuaContent(), keys, args.toArray(new String[0])), "消息持久化失败");
    }

    private long genDelayMessageScore(long delay) {
        Preconditions.checkArgument(delay > 0 && Duration.ofMillis(delay).toDays() <= 30,
                "延迟消息延迟时间 0 < delay < 30 days");
        return System.currentTimeMillis() + delay;
    }
}
