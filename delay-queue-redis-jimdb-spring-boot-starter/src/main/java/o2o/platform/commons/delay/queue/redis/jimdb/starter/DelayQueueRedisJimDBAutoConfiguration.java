package o2o.platform.commons.delay.queue.redis.jimdb.starter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.CaseFormat;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jd.jim.cli.Cluster;

import o2o.platform.commons.delay.queue.redis.core.domain.DelayQueueConsumerProcessor;
import o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionHandler;
import o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionReentrantHandler;
import o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerExceptionRetryHandler;
import o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerHandler;
import o2o.platform.commons.delay.queue.redis.core.handler.DelayQueueConsumerSkipHandler;
import o2o.platform.commons.delay.queue.redis.core.properties.DelayQueueProperties;
import o2o.platform.commons.delay.queue.redis.core.properties.ThreadPoolExecutorProperties;
import o2o.platform.commons.delay.queue.redis.core.redis.RedisKeyResolver;
import o2o.platform.commons.delay.queue.redis.core.redis.RedisOpService;
import o2o.platform.commons.delay.queue.redis.core.service.DelayMessageProducer;
import o2o.platform.commons.delay.queue.redis.core.service.DelayQueueConsumer;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@Configuration
public class DelayQueueRedisJimDBAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "delay.queue")
    public DelayQueueProperties delayQueueProperties() {
        return new DelayQueueProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisKeyResolver redisKeyResolver(DelayQueueProperties delayQueueProperties) {
        return new RedisKeyResolver(delayQueueProperties.getAppName(), delayQueueProperties.getEnv());
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisOpService redisOpService(Cluster cluster) {
        return new JimDBRedisTemplate(cluster);
    }


    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolExecutor consumerTaskExecutor(DelayQueueProperties delayQueueProperties) {
        ThreadPoolExecutorProperties executor = delayQueueProperties.getExecutor();
        return new ThreadPoolExecutor(executor.getCoreSize(), executor.getMaxSize(),
                1, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(executor.getQueueSize()),
                new ThreadFactoryBuilder().setNameFormat(executor.getThreadNamePrefix()).build(),
                new CallerRunsPolicy());
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayMessageProducer p(RedisOpService redisOpService, RedisKeyResolver redisKeyResolver,
            DelayQueueProperties delayQueueProperties) {
        return new DelayMessageProducer(redisOpService, redisKeyResolver, delayQueueProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayQueueConsumerExceptionReentrantHandler reentrantDelayQueueExceptionHandler(
            DelayMessageProducer delayMessageProducer,
            DelayQueueProperties delayQueueProperties) {
        return new DelayQueueConsumerExceptionReentrantHandler(delayMessageProducer, delayQueueProperties);
    }


    @Bean
    @ConditionalOnMissingBean
    public DelayQueueConsumerExceptionRetryHandler retryDelayQueueExceptionHandler(
            DelayMessageProducer delayMessageProducer,
            DelayQueueProperties delayQueueProperties) {
        return new DelayQueueConsumerExceptionRetryHandler(delayMessageProducer, delayQueueProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DelayQueueConsumerProcessor injectDelayQueueConsumeHandler(
            List<DelayQueueConsumerHandler> delayQueueConsumerHandlerList,
            List<DelayQueueConsumerExceptionHandler> consumeExceptionHandlerList) {

        Map<String, DelayQueueConsumerHandler> consumeHandlerMap = delayQueueConsumerHandlerList
                .stream()
                .collect(Collectors.toMap(
                        handler -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
                                handler.getClass().getSimpleName()),
                        Function.identity(), (a, b) -> a));

        Map<String, DelayQueueConsumerExceptionHandler> consumeExceptionHandlerMap = consumeExceptionHandlerList
                .stream()
                .collect(Collectors.toMap(
                        handler -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
                                handler.getClass().getSimpleName()),
                        Function.identity(), (a, b) -> a));

        DelayQueueConsumerProcessor processor = new DelayQueueConsumerProcessor();
        processor.setConsumerHandlerMap(consumeHandlerMap);
        processor.setConsumerExceptionHandlerMap(consumeExceptionHandlerMap);
        return processor;
    }


    @Bean
    @ConditionalOnMissingBean
    public DelayQueueConsumerSkipHandler emptyDelayMessageConsumerHandler() {
        return new DelayQueueConsumerSkipHandler();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public DelayQueueConsumer c(RedisOpService redisOpService, RedisKeyResolver redisKeyResolver,
            DelayQueueProperties delayQueueProperties, ThreadPoolExecutor consumerThreadExecutor,
            DelayQueueConsumerProcessor delayQueueConsumerProcessor) {
        return new DelayQueueConsumer(redisOpService, redisKeyResolver, delayQueueProperties, consumerThreadExecutor,
                delayQueueConsumerProcessor);
    }


}
