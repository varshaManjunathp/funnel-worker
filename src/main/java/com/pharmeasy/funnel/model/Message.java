package com.pharmeasy.funnel.model;

import lombok.*;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String id;
    private String segmentId;
    private int currentRetryCount;
    private int maxRetryCount;
    private boolean isNew = true;
    private ZonedDateTime createTime;
    private ZonedDateTime updateTime;
    private String statusCode;

}
