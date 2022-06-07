package o2o.platform.commons.delay.queue.redis.core.monitor;

/**
 * 空实现
 *
 * @author zhouyang01
 * Created on 2022-06-07
 */
public class EmptyMetricService implements MetricService {
    @Override
    public void count(String name, Object... tags) {

    }

    @Override
    public void count(int delta, String name, Object... tags) {

    }

    @Override
    public void time(long elapsedTime, String name, Object... tags) {

    }
}
