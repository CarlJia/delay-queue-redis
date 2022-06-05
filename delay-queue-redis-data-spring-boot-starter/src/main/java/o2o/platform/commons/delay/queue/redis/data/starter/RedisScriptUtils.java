package o2o.platform.commons.delay.queue.redis.data.starter;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import o2o.platform.commons.delay.queue.redis.core.redis.RedisLuaUtils;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@SuppressWarnings("ALL")
public class RedisScriptUtils {

    private static final RedisScript<Long> persistentScript;
    private static final RedisScript<List> fetchLuaScript;

    static {
        //构建持久化对象
        persistentScript = new DefaultRedisScript<>(RedisLuaUtils.getPersistentLuaContent(), Long.class);

        //构建
        fetchLuaScript = new DefaultRedisScript<>(RedisLuaUtils.getFetchLuaContent(), List.class);
    }


    public static RedisScript<Long> getPersistentScript() {
        checkArgument(persistentScript != null, "persistent redis script is null");
        return persistentScript;
    }

    public static RedisScript<List> getFetchScript() {
        checkArgument(fetchLuaScript != null, "fetch redis script is null");
        return fetchLuaScript;
    }
}
