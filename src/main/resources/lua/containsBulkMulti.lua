local default = 0;
    local results = {};
    for i, segid in ipairs (ARGV)
    do
        local qType = redis.call('type', segid);
        if type(qType) == 'table'
        then
            qType = qType.ok
        end
        if qType == 'set'
        then
            table.insert(results, redis.call('SISMEMBER', segid, KEYS[1]))
        elseif qType == 'string'
        then
            table.insert(results, redis.call('GETBIT', segid, KEYS[1]))
        else
            table.insert(results, default)
        end
    end
    return results