local default = "0";
local results = {};
for i, userid in ipairs (ARGV)
do
    local qType = redis.call('type', KEYS[1]);
    if type(qType) == 'table'
    then
        qType = qType.ok
    end
    if qType == 'set'
    then
        table.insert(results, redis.call('SISMEMBER', KEYS[1], userid))
    elseif qType == 'string'
    then
        table.insert(results, redis.call('GETBIT', KEYS[1], userid))
    else
        table.insert(results, default)
    end
end
return results