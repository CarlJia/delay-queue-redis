package o2o.platform.commons.delay.queue.redis.core.redis;

import java.util.List;
import java.util.Set;

/**
 * redis缓存处理
 *
 * @author zhouyang01
 * Created on 2022-06-04
 */
public interface RedisOpService {


    /**
     * 消息持久 (hset、zadd)
     */
    boolean persist(String luaContent, List<String> keys, String... args);


    /**
     * 获取消息(zrem、hget、hel)
     */
    List<String> fetch(String luaContent, List<String> keys, String... args);

    /**
     * 有序集合扫描(扫描指定数据)
     * <a href="http://redisdoc.com/sorted_set/zrangebyscore.html">zrangebyscore</a>
     */
    Set<String> zSetRangeByScore(String key, long min, long max, int offset, int count);

    /**
     * 有序集合扫描（扫描全部）
     * <a href="http://redisdoc.com/sorted_set/zrangebyscore.html">zrangebyscore</a>
     */
    Set<String> zSetRangeByScore(String key, long min, long max);
}
