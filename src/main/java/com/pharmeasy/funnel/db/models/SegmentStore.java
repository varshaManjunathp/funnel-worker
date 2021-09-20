package com.pharmeasy.funnel.db.models;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
public class SegmentStore {
    @Id
    @Column(name = "segment_id")
    private int segmentId;

    @Column(name = "entity")
    private String entity;

    @Column(name = "entity_id")
    private String entityID;

    @Column(name = "source")
    private String source;

    @Column(name = "transaction")
    private String transaction;
}
