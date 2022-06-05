package o2o.platform.commons.delay.queue.redis.data.starter;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import o2o.platform.commons.delay.queue.redis.core.redis.RedisOpService;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
public class SpringRedisTemplate extends RedisTemplate<String, String> implements RedisOpService {

    public SpringRedisTemplate() {
        // 所有的key value 都按照字符串来处理
        setKeySerializer(RedisSerializer.string());
        setValueSerializer(RedisSerializer.string());

        setHashKeySerializer(RedisSerializer.string());
        setHashValueSerializer(RedisSerializer.string());
    }

    public SpringRedisTemplate(RedisConnectionFactory factory) {
        this();
        setConnectionFactory(factory);
        afterPropertiesSet();
    }

    @Override
    public boolean persist(String luaContent, List<String> keys, String... args) {
        Long execute = execute(RedisScriptUtils.getPersistentScript(), keys, args);
        return execute != null && execute > 0;
    }

    @Override
    public List<String> fetch(String luaContent, List<String> keys, String... args) {
        return execute(RedisScriptUtils.getFetchScript(), keys, args);
    }

    @Override
    public Set<String> zSetRangeByScore(String key, long min, long max, int offset, int count) {
        return opsForZSet().rangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> zSetRangeByScore(String key, long min, long max) {
        return opsForZSet().rangeByScore(key, min, max);
    }
}
