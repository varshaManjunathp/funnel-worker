for i, arg in ipairs (ARGV)
	do
		redis.call('setbit', KEYS[1], arg, 1)
	end
	return "0"