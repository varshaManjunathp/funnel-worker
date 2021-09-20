for i, arg in ipairs (ARGV)
    do
        local qType = redis.call('type', KEYS[1]);
        if type(qType) == 'table'
        then
            qType = qType.ok
        end
        if qType == 'set'
        then
            redis.call('SREM', KEYS[1], arg)
        elseif qType == 'string'
        then
            redis.call('SETBIT', KEYS[1], arg, 0)
        end
    end
    return 0