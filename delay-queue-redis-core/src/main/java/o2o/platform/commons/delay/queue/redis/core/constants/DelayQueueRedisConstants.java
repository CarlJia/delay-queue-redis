package o2o.platform.commons.delay.queue.redis.core.constants;

import java.time.Duration;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
public class DelayQueueRedisConstants {
    public static final long MAX_DELAY_MILLS = Duration.ofDays(7).toMillis();
}
