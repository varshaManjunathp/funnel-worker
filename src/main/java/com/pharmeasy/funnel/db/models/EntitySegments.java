package com.pharmeasy.funnel.db.models;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "entity_segments")
public class EntitySegments {
    @Id
    @Column(name="ROWID")
    String rowId;

    private String entity;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "segment_id")
    private int segmentId;

    public EntitySegments(String entity, String entityID, int segmentId) {
        this.entity = entity;
        this.entityId = entityID;
        this.segmentId = segmentId;
    }
}
