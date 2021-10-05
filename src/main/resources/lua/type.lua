local segType =  redis.call('type', KEYS[1])["ok"]
if segType == "none" then
    return false
else
    return true
end