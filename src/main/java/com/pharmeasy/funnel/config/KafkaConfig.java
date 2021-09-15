//package com.pharmeasy.funnel.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.SendResult;
//import org.springframework.stereotype.Service;
//import org.springframework.util.concurrent.ListenableFuture;
//
//import java.io.IOException;
//
//@Configuration
//@ConfigurationProperties(prefix = "spring.kafka.funnel")
//public class KafkaConfig {
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    String topicName;
//    String groupName;
//
//    public void send(String message) {
//        this.kafkaTemplate.send(topicName, message);
//    }
//
//    @KafkaListener(topics = "${spring.kafka.funnel.topicName}", groupId = "${spring.kafka.funnel.groupName}")
//    public void consume(String message) throws IOException {
//    }
//}
