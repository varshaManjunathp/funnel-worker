package com.pharmeasy.funnel;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRedisRepositories
@EnableAutoConfiguration
@EnableScheduling
public class FunnelApplication {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		SpringApplication.run(FunnelApplication.class, args);
	}

}
