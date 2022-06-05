--延迟消息数据批量获取

local hash_key = KEYS[1];
local hash_field = KEYS[2];

local bucket_key = KEYS[3];
local bucket_number = ARGV[1];

local result = {};

if redis.call('ZREM', bucket_key, bucket_number) > 0 then
    local message = redis.call('HGET', hash_key, hash_field);
    table.insert(result, message);
    redis.call('HDEL', hash_key, hash_field);
end

return result;