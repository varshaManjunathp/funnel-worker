package com.pharmeasy.funnel.db.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "entity_segments")
@IdClass(EntitySegmentsId.class)
@NoArgsConstructor
public class EntitySegments {

    @Id
    private String entity;

    @Id
    @Column(name = "entity_id")
    private String entityId;

    @Id
    @Column(name = "segment_id")
    private long segmentId;


    public EntitySegments(String entity, String entityID, int segmentId) {
        this.entity = entity;
        this.entityId = entityID;
        this.segmentId = segmentId;
    }
}

class EntitySegmentsId implements Serializable {
    private String entity;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "segment_id")
    private long segmentId;
}
