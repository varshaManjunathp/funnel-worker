package com.pharmeasy.funnel.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmeasy.funnel.model.SegmentMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;


@Component
@AllArgsConstructor
@NoArgsConstructor
public class RedisQueueConsumer {

    private RedisTemplate<String, SegmentMessage> consumerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Resource(name="consumerTemplate")
    private ZSetOperations<String, String> listOperations;

    @Value("${env}:funnel:worker:segments:delayed")
    private String key;

    public Set<String> readJobFromQueue() throws JsonProcessingException {
            long size = listOperations.size(key);
            Set<String> value = listOperations.range(key, 0 , size);
            return value;
    }
}
