package com.pharmeasy.funnel.utils;

import com.pharmeasy.funnel.service.SegmentUpdateService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ScheduleSegmentUpdate {

    SegmentUpdateService segmentUpdateService;

    @Scheduled(cron = "0 0 9,18 * * *")
    public void run() {
        segmentUpdateService.updateSegments();
    }
}
