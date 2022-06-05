package o2o.platform.commons.delay.queue.redis.data.starter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(DelayQueueRedisDataAutoConfiguration.class)
public @interface EnabledDelayQueue {
}
