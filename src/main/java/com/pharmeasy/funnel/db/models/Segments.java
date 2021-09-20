package com.pharmeasy.funnel.db.models;

import com.pharmeasy.funnel.model.SegmentStatus;
import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "segments")
public class Segments {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    private String name;
    private String query;
    private SegmentStatus status;
    private String type;

    @Column(name = "cron_string")
    private String cronString;

    private String metadata;
    private String storage;

    @Column(name = "parent_segment_id")
    private String parentSegmentId;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

}
