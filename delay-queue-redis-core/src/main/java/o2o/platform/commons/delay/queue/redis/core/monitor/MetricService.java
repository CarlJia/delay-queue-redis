package o2o.platform.commons.delay.queue.redis.core.monitor;

/**
 * 监控
 *
 * @author zhouyang01
 * Created on 2022-06-07
 */
public interface MetricService {

    void count(String name, Object... tags);

    void count(int delta, String name, Object... tags);

    void time(long elapsedTime, String name, Object... tags);
}
