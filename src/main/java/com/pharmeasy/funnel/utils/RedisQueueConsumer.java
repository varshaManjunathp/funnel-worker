package com.pharmeasy.funnel.utils;

import com.pharmeasy.funnel.model.SegmentMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;


@Component
@AllArgsConstructor
@NoArgsConstructor
public class RedisQueueConsumer {

    private RedisTemplate<String, SegmentMessage> consumerTemplate;

    @Resource(name="consumerTemplate")
    private ZSetOperations<String, String> zSetOperations;

    @Value("${env}:funnel:worker:segments:delayed")
    private String key;

    public Set<String> readJobFromQueue() {
            long size = zSetOperations.size(key);
            Set<String> value = zSetOperations.range(key, 0 , size);
            return value;
    }
}
