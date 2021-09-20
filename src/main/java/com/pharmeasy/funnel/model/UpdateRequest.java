package com.pharmeasy.funnel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
public class UpdateRequest {

    @Nonnull
    private String segmentId;

    @JsonFormat(pattern="dd-MM-yyyy hh:mm:ss")
    private LocalDateTime lastLogin;
}
