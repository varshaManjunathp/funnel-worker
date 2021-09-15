package com.pharmeasy.funnel.db.models;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "segment_store")
@Data
public class SegmentStore {
    @Id
    private int segmentId;
    private String entity;
    private String entityID;
    private String source;
    private String transaction;
}
