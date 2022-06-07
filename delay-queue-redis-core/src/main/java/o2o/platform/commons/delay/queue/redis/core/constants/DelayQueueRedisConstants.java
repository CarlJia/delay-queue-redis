package o2o.platform.commons.delay.queue.redis.core.constants;

import java.time.Duration;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
public class DelayQueueRedisConstants {
    public static final long MAX_DELAY_MILLS = Duration.ofDays(7).toMillis();

    public static final String METRIC_PRODUCER = "delay_queue_redis_produce";
    public static final String METRIC_CONSUMER = "delay_queue_redis_consume";

    public static final String TAG_RESULT = "result";
    public static final String TAG_TOPIC = "topic";
    public static final String TAG_CONSUMER_GRAB = "grab";
    public static final String TAG_CONSUMER_RETRY = "retryHandler";

    public static final String RET_SUCCESS = "success";
    public static final String RET_FAIL = "failure";
    public static final String RET_FAIL_CONSUMER_INNER = "innerError";
}
