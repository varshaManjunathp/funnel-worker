package com.pharmeasy.funnel.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties(prefix = "spring.hive")
public class HiveConfig {

    private String url;
    private String username;
    private String password;
    private JdbcTemplate jdbcTemplate;

    @Value("${env}")
    private String environment;

    public DataSource hiveDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName("org.apache.hive.jdbc.HiveDriver");
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate(){
         this.jdbcTemplate =  new JdbcTemplate(hiveDataSource());
         migrate();
         return jdbcTemplate;
    }

    public void migrate() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS funnel.segment_store_"+ environment +
                " (\n" +
                "\tsegment_id INTEGER   \n" +
                "\t,entity VARCHAR(256) \n" +
                "\t,entity_id VARCHAR(256)  \n" +
                "\t,source VARCHAR(256)  \n" +
                "\t,transaction VARCHAR(256)  \n" +
                ")\n");
    }

}
