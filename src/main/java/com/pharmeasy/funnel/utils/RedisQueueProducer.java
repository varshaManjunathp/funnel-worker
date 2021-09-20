package com.pharmeasy.funnel.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmeasy.funnel.model.SegmentMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.ZonedDateTime;
import java.util.UUID;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class RedisQueueProducer {

    private RedisTemplate<String, SegmentMessage> publisherTemplate;
    private ChannelTopic topic;

    private ObjectMapper objectMapper;

    @Value("${env}:funnel:worker:segments:delayed")
    private String key;

    @Resource(name="publisherTemplate")
    private ListOperations<String, String> listOperations;

    public boolean sendJobToQueue(SegmentMessage message) {
        try {
            if (message != null) {
                if (StringUtils.isEmpty(message.getId())) {
                    message.setId(UUID.randomUUID().toString());
                }
                message.setUpdateTime(ZonedDateTime.now());

                listOperations.rightPush(key, objectMapper.writeValueAsString(message));
                return true;
            }
        } catch(JsonProcessingException e) {
            return false;
        }
        return false;
    }
}
