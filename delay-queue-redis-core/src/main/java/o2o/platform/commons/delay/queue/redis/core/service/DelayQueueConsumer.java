package o2o.platform.commons.delay.queue.redis.core.service;

import static o2o.platform.commons.delay.queue.redis.core.redis.RedisLuaUtils.getFetchLuaContent;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import o2o.platform.commons.delay.queue.redis.core.domain.DelayQueueConsumerProcessor;
import o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionHandler;
import o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerHandler;
import o2o.platform.commons.delay.queue.redis.core.message.DelayMessage;
import o2o.platform.commons.delay.queue.redis.core.properties.ConsumeProperties;
import o2o.platform.commons.delay.queue.redis.core.properties.DelayQueueProperties;
import o2o.platform.commons.delay.queue.redis.core.properties.TopicProperties;
import o2o.platform.commons.delay.queue.redis.core.redis.RedisKeyResolver;
import o2o.platform.commons.delay.queue.redis.core.redis.RedisOpService;
import o2o.platform.commons.delay.queue.redis.core.util.Jsons;

/**
 * @author zhouyang01
 * Created on 2022-06-04
 */
public class DelayQueueConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DelayQueueConsumer.class);
    private final RedisOpService redisOpService;
    private final RedisKeyResolver redisKeyResolver;
    private final DelayQueueProperties delayQueueProperties;
    private final ThreadPoolExecutor consumerThreadExecutor;
    private final Map<String, DelayQueueConsumerHandler> consumeHandlerMap;
    private final Map<String, DelayQueueConsumerExceptionHandler> consumeExceptionHandlerMap;

    /**
     * 消息服务开启状态
     */
    private final AtomicBoolean started = new AtomicBoolean(false);
    /**
     * 消息处理状态
     */
    private final AtomicBoolean processing = new AtomicBoolean(false);

    private ScheduledThreadPoolExecutor scheduledExecutor;

    public DelayQueueConsumer(RedisOpService redisOpService, RedisKeyResolver redisKeyResolver,
            DelayQueueProperties delayQueueProperties, ThreadPoolExecutor consumerThreadExecutor,
            DelayQueueConsumerProcessor delayQueueConsumerProcessor) {
        this.redisOpService = redisOpService;
        this.redisKeyResolver = redisKeyResolver;
        this.delayQueueProperties = delayQueueProperties;
        this.consumerThreadExecutor = consumerThreadExecutor;
        this.consumeHandlerMap = delayQueueConsumerProcessor.getConsumerHandlerMap();
        this.consumeExceptionHandlerMap = delayQueueConsumerProcessor.getConsumerExceptionHandlerMap();

    }


    /**
     * 开启服务
     */
    public void start() {
        ConsumeProperties consumeProperties = Objects.requireNonNull(delayQueueProperties.getC());
        if (consumeProperties.isEnabled() && started.compareAndSet(false, true)) {
            if (scheduledExecutor == null) {
                scheduledExecutor = new ScheduledThreadPoolExecutor(1,
                        new ThreadFactoryBuilder().setNameFormat("delay-queue-consumer-scan-").build(),
                        new ThreadPoolExecutor.CallerRunsPolicy());
            }
            scheduledExecutor.scheduleAtFixedRate(this::start0,
                    consumeProperties.getInitialDelay().getSeconds(),
                    consumeProperties.getScanInterval().getSeconds(),
                    TimeUnit.SECONDS);

        }
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        shutdown0();
    }


    @SuppressWarnings("rawtypes")
    private void start0() {
        if (started.get() && !processing.getAndSet(true)) {
            consumerThreadExecutor.execute(() -> {
                Map<String, TopicProperties> topicMap = delayQueueProperties.getTopics();
                if (topicMap == null || topicMap.isEmpty()) {
                    logger.warn("无待消息的topic配置 {}", delayQueueProperties);
                    return;
                }
                CompletableFuture[] completableFutures = topicMap.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().isEnabled())
                        .map(entry -> CompletableFuture.runAsync(
                                () -> processTopic(entry.getKey()), consumerThreadExecutor))
                        .toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(completableFutures).join();
                //当前批次处理完毕，设置处理中状态为false
                processing.set(false);
            });
        } else {
            logger.warn("本轮消费未完成，等待下次调度");
        }
    }

    private void processTopic(String topic) {
        Set<String> messageIds = fetchMessageIds(topic);
        if (messageIds == null || messageIds.size() == 0) {
            logger.info("topic={} 无待处理的消息", topic);
            return;
        }
        messageIds.forEach(
                messageId ->
                        consumerThreadExecutor.execute(() ->
                                processMessage(topic, messageId)));
    }


    private Set<String> fetchMessageIds(String topic) {
        String bucketKey = redisKeyResolver.bucketKey(topic);
        long maxScore = System.currentTimeMillis();
        int fetchSize = delayQueueProperties.getTopics().get(topic).getFetchSize();
        if (fetchSize > 0) {
            return redisOpService.zSetRangeByScore(bucketKey, 0, maxScore, 0, fetchSize);
        }
        return redisOpService.zSetRangeByScore(bucketKey, 0, maxScore);
    }

    private void processMessage(String topic, String messageId) {
        DelayMessage delayMessage = fetchDelayMessage(topic, messageId);
        if (delayMessage == null) {
            return;
        }
        try {
            consumeHandlerMap.get(delayQueueProperties.getTopics().get(topic).getConsumerHandlerName())
                    .onMessage(delayMessage);
        } catch (Exception e) {
            consumeExceptionHandlerMap.get(
                            delayQueueProperties.getTopics().get(topic).getConsumerExceptionHandlerName())
                    .onException(delayMessage, e);
        }
    }

    private DelayMessage fetchDelayMessage(String topic, String messageId) {
        List<String> keys = Lists.newArrayListWithCapacity(3);
        keys.add(redisKeyResolver.hashKey(topic));
        keys.add(redisKeyResolver.key(topic, messageId));
        keys.add(redisKeyResolver.bucketKey(topic));

        List<String> messageList = redisOpService.fetch(getFetchLuaContent(), keys, messageId);
        //已经被其他进程抢到
        if (messageList == null || messageList.size() == 0) {
            return null;
        }
        String message = messageList.get(0);
        return Jsons.fromJson(message, DelayMessage.class);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void shutdown0() {
        if (!scheduledExecutor.isShutdown()) {
            started.set(false);
            scheduledExecutor.shutdown();
            long shutdownAwaitTime = delayQueueProperties.getC().getShutdownAwaitTime().getSeconds();
            boolean termination =
                    MoreExecutors.shutdownAndAwaitTermination(scheduledExecutor, shutdownAwaitTime, TimeUnit.SECONDS);
            logger.info("delay queue consumer is shutdown(scheduledExecutor), termination={}", termination);
        }

        if (!consumerThreadExecutor.isShutdown()) {
            processing.set(true);
            Duration shutdownAwaitTime = delayQueueProperties.getExecutor().getShutdownAwaitTime();
            boolean termination =
                    MoreExecutors.shutdownAndAwaitTermination(consumerThreadExecutor, shutdownAwaitTime.getSeconds(),
                            TimeUnit.SECONDS);
            logger.info("delay queue consumer is shutdown(consumerThreadExecutor), termination={}", termination);
        }
    }
}
