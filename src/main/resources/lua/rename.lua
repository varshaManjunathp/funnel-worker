for i, arg in ipairs (ARGV)
    do
        local qType = redis.call('type', KEYS[1]);
        if type(qType) == 'table'
        then
            qType = qType.ok
        end
        if qType == 'none'
        then
            return 0
        else
            redis.call('rename', KEYS[1], arg)
        end
    end
    return 0