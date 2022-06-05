--延迟消息持久化处理

-- print(KEYS[1])
-- print(KEYS[2])
-- print(KEYS[3])
-- print(ARGV[1])
-- print(ARGV[2])
-- print(ARGV[3])

local hash_result = redis.call('HSET', KEYS[1], KEYS[2], ARGV[1]);

local bucket_result = redis.call('ZADD', KEYS[3], tonumber(ARGV[2]), ARGV[3]);

return hash_result + bucket_result;
