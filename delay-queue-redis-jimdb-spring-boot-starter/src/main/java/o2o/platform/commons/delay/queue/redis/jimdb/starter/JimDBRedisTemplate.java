package o2o.platform.commons.delay.queue.redis.jimdb.starter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;
import com.jd.jim.cli.Cluster;

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
