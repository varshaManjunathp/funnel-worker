package com.pharmeasy.funnel.model;

import lombok.Data;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;

@Data
public class UpdateRequest {

    @Nonnull
    private String segmentId;
    private ZonedDateTime scheduleTime;
}
