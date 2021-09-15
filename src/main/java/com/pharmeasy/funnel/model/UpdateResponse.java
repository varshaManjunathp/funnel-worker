package com.pharmeasy.funnel.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Builder
@Getter
public class UpdateResponse {

    @DateTimeFormat
    @LastModifiedDate
    LocalDateTime time;

    Status status;
}
