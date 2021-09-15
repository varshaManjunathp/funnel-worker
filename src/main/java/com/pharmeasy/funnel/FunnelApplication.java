package com.pharmeasy.funnel;

import com.pharmeasy.funnel.db.migration.SegmentStoreMigration;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableRedisRepositories
public class FunnelApplication {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		SpringApplication.run(FunnelApplication.class, args);
	}

}
