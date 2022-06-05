package o2o.platform.commons.delay.queue.redis.core.redis;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Resources.getResource;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
@SuppressWarnings("UnstableApiUsage")
public class RedisLuaUtils {

    private static final String persistentLuaContent;
    private static final String fetchLuaContent;

    static {
        try {
            persistentLuaContent = Resources.toString(getResource("lua/delay_queue_persist.lua"), UTF_8);
            fetchLuaContent = Resources.toString(getResource("lua/delay_queue_fetch.lua"), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPersistentLuaContent() {
        Preconditions.checkArgument(!isNullOrEmpty(persistentLuaContent));
        return persistentLuaContent;
    }

    public static String getFetchLuaContent() {
        Preconditions.checkArgument(!isNullOrEmpty(fetchLuaContent));
        return fetchLuaContent;
    }
}
