package com.pharmeasy.funnel.db.repository;

import com.pharmeasy.funnel.db.models.EntitySegments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntityRepository  extends JpaRepository<EntitySegments, Long> {
    int deleteBySegmentId(long id);
}
