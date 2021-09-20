package com.pharmeasy.funnel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SegmentMessage {

    private String id;
    private String message;

    @JsonProperty("current_retry")
    private int currentRetryCount;

    @JsonProperty("retries")
    private int maxRetryCount;

    @JsonProperty("is_new")
    private boolean isNew = true;
    private ZonedDateTime createTime;

    private String cron;

    @JsonProperty("last_updated_on")
    private ZonedDateTime updateTime;
    private String statusCode;

}
