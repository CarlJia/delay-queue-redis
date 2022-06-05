package o2o.platform.commons.delay.queue.redis.core.redis;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * redis key 相关处理
 *
 * @author zhouyang01
 * Created on 2022-06-04
 */
public class RedisKeyResolver {

    private String appName;

    public String getAppName() {
        return appName;
    }

    public RedisKeyResolver(String appName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appName), "appName is null");
        this.appName = appName;
    }

    /**
     * hash key
     */
    public String hashKey(String topic) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(topic), "topic is null");
        return String.format("delay.queue.hash.%s.{%s}", this.appName, topic);
    }

    /**
     * normal key
     */
    public String key(String topic, String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(topic), "topic is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "key is null");
        return String.format("{%s}.%s", topic, key);
    }

    /**
     * bucket key
     */
    public String bucketKey(String topic) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(topic), "topic is null");
        return String.format("delay.queue.bucket.%s.{%s}", this.appName, topic);
    }
}
