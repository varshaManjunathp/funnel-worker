local result;
    for i, userid in ipairs (ARGV)
    do
        local qType = redis.call('type', KEYS[1]);
        if type(qType) == 'table'
        then
            qType = qType.ok
        end
        if qType == 'set'
        then
            result = redis.call('SISMEMBER', KEYS[1], userid)
        elseif qType == 'string'
        then
            result = redis.call('GETBIT', KEYS[1], userid)
        else
            result = 0
        end
    end
    return result