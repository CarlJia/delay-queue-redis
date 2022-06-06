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

    private final String appName;
    private final String env;

    public RedisKeyResolver(String appName, String env) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appName), "appName is null");
        this.appName = appName;
        this.env = Strings.nullToEmpty(env);
    }

    /**
     * hash key
     */
    public String hashKey(String topic) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(topic), "topic is null");
        return String.format("%s-%s-{%s}-delay-queue-hash", this.appName, this.env, topic);
    }

    /**
     * normal key
     */
    public String key(String topic, String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(topic), "topic is null");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "key is null");
        return String.format("%s-{%s}-%s", this.env, topic, key);
    }

    /**
     * bucket key
     */
    public String bucketKey(String topic) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(topic), "topic is null");
        return String.format("%s-%s-{%s}-delay-queue-bucket", this.appName, this.env, topic);
    }
}
