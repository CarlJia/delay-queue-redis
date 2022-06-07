package o2o.platform.commons.delay.queue.redis.core.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static o2o.platform.commons.delay.queue.redis.core.constants.DelayQueueRedisConstants.MAX_DELAY_MILLS;
import static o2o.platform.commons.delay.queue.redis.core.constants.DelayQueueRedisConstants.METRIC_PRODUCER;
import static o2o.platform.commons.delay.queue.redis.core.constants.DelayQueueRedisConstants.TAG_RESULT;
import static o2o.platform.commons.delay.queue.redis.core.constants.DelayQueueRedisConstants.TAG_TOPIC;
import static o2o.platform.commons.delay.queue.redis.core.constants.ResultStatus.SEND_ENABLE_CLOSED;
import static o2o.platform.commons.delay.queue.redis.core.domain.SendResult.failure;
import static o2o.platform.commons.delay.queue.redis.core.domain.SendResult.success;
import static o2o.platform.commons.delay.queue.redis.core.redis.RedisLuaUtils.getPersistentLuaContent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import o2o.platform.commons.delay.queue.redis.core.constants.ResultStatus;
import o2o.platform.commons.delay.queue.redis.core.domain.SendResult;
import o2o.platform.commons.delay.queue.redis.core.message.DelayMessage;
import o2o.platform.commons.delay.queue.redis.core.monitor.MetricService;
import o2o.platform.commons.delay.queue.redis.core.properties.DelayQueueProperties;
import o2o.platform.commons.delay.queue.redis.core.redis.RedisKeyResolver;
import o2o.platform.commons.delay.queue.redis.core.redis.RedisOpService;

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
    private final MetricService metricService;

    public DelayMessageProducer(RedisOpService redisOpService, RedisKeyResolver redisKeyResolver,
            DelayQueueProperties delayQueueProperties, MetricService metricService) {
        this.redisOpService = redisOpService;
        this.redisKeyResolver = redisKeyResolver;
        this.delayQueueProperties = delayQueueProperties;
        this.metricService = metricService;
    }

    /**
     * 发送消息至延迟队列，最大延迟时间为
     * {@link o2o.platform.commons.delay.queue.redis.core.constants.DelayQueueRedisConstants#MAX_DELAY_MILLS}
     */
    public SendResult send(DelayMessage delayMessage) {
        if (!delayQueueProperties.getP().isEnabled()) {
            return failure(SEND_ENABLE_CLOSED, "发送消息开关为关闭状态");
        }
        logger.info("send delay message, {}", delayMessage);
        SendResult sendResult = success(delayMessage.getMessageId());
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            checkDelayMessage(delayMessage);
            sendDelayMessage(delayMessage);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                sendResult = failure(ResultStatus.SEND_INVALID_PARAMETERS, e.getMessage());
            } else {
                sendResult = failure(ResultStatus.SEND_FAILURE, e.getMessage());
            }
            logger.error("send delay message error, {}", JSON.toJSONString(delayMessage), e);
        } finally {
            Object[] tags = {TAG_TOPIC, delayMessage.getTopic(), TAG_RESULT, sendResult.getResultStatus()};
            metricService.time(stopwatch.elapsed(MILLISECONDS), METRIC_PRODUCER, tags);
        }
        return sendResult;
    }


    private void checkDelayMessage(DelayMessage delayMessage) {
        checkArgument(delayMessage != null, "延迟消息对象为空");
        String topic = delayMessage.getTopic();
        checkArgument(!isNullOrEmpty(topic), "延迟消息 Topic 为空");
        checkArgument(delayQueueProperties.getTopics().containsKey(topic), "延迟消息 Topic 没有配置对应 Consumer 配置");
        checkArgument(delayMessage.getDelay() > 0 && delayMessage.getDelay() < MAX_DELAY_MILLS,
                "延迟消息 Delay 必须 > 0 && < " + ofMillis(MAX_DELAY_MILLS).toDays() + " days");
        checkArgument(delayMessage.getPayload() != null, "延迟消息内容不能为空");
    }

    private void sendDelayMessage(DelayMessage delayMessage) {
        List<String> keys = Lists.newArrayListWithCapacity(3);
        keys.add(redisKeyResolver.hashKey(delayMessage.getTopic()));
        keys.add(redisKeyResolver.key(delayMessage.getTopic(), delayMessage.getMessageId()));
        keys.add(redisKeyResolver.bucketKey(delayMessage.getTopic()));

        List<String> args = Lists.newArrayListWithCapacity(3);
        args.add(JSON.toJSONString(delayMessage));
        args.add(String.valueOf(genDelayMessageScore(delayMessage.getDelay())));
        args.add(delayMessage.getMessageId());
        checkState(redisOpService.persist(getPersistentLuaContent(), keys, args.toArray(new String[0])),
                "消息持久化失败");
    }

    private long genDelayMessageScore(long delay) {
        Preconditions.checkArgument(delay > 0 && delay < MAX_DELAY_MILLS,
                "延迟消息延迟时间 0 < delay <= " + ofMillis(MAX_DELAY_MILLS).toDays() + " days");
        return System.currentTimeMillis() + delay;
    }
}
