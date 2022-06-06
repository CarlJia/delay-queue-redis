package o2o.platform.commons.delay.queue.redis.jimdb.starter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.util.ReflectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.jd.jim.cli.Cluster;
import com.jd.jim.cli.shard.KeyShardEncoder;

import o2o.platform.commons.delay.queue.redis.core.redis.RedisLuaUtils;
import o2o.platform.commons.delay.queue.redis.core.redis.RedisOpService;

/**
 * @author zhouyang01
 * Created on 2022-06-05
 */
public class JimDBRedisTemplate implements RedisOpService {
    private final Cluster cluster;
    private volatile String persistentSha;
    private volatile String fetchSha;

    public JimDBRedisTemplate(Cluster cluster) {
        this.cluster = cluster;
        Preconditions.checkArgument(cluster != null, "构建jimDBCluster对象失败");

        //服务启动的后，直接上传lua脚本至jimDB server
        persistentSha = cluster.scriptLoad(RedisLuaUtils.getPersistentLuaContent());
        fetchSha = cluster.scriptLoad(RedisLuaUtils.getFetchLuaContent());

        //临时方案，直接把keyShardEncoder设置为支持hashtag.只适合当前jimDB确实已经支持hashtag。同时jimDB客户端升级的时候需要关注下
        KeyShardEncoder shardEncoder = new KeyShardEncoder("UTF-8", true);
        ReflectionUtils.findField(cluster.getClass(), "shardEncoder");
        Field shardEncoderField = ReflectionUtils.findField(cluster.getClass(), "shardEncoder", KeyShardEncoder.class);
        ReflectionUtils.makeAccessible(shardEncoderField);
        ReflectionUtils.setField(shardEncoderField, cluster, shardEncoder);
    }

    @Override
    public boolean persist(String luaContent, List<String> keys, String... args) {
        if (Strings.isNullOrEmpty(persistentSha)) {
            persistentSha = cluster.scriptLoad(luaContent);
        }
        Object eval = cluster.evalsha(persistentSha, keys, Arrays.asList(args), false);
        return eval != null;
    }

    @Override
    public List<String> fetch(String luaContent, List<String> keys, String... args) {
        if (Strings.isNullOrEmpty(fetchSha)) {
            fetchSha = cluster.scriptLoad(luaContent);
        }
        Object eval = cluster.evalsha(fetchSha, keys, Arrays.asList(args), false);
        return (List<String>) eval;
    }

    @Override
    public Set<String> zSetRangeByScore(String key, long min, long max, int offset, int count) {
        return cluster.zRangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> zSetRangeByScore(String key, long min, long max) {
        return cluster.zRangeByScore(key, min, max);
    }
}
