package com.pharmeasy.funnel.db.migration;

import com.pharmeasy.funnel.db.repository.SegmentStoreRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;

@AllArgsConstructor
@NoArgsConstructor
public class SegmentStoreMigration {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("environment")
    private String environment;

}
