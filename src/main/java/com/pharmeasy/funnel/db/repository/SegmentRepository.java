package com.pharmeasy.funnel.db.repository;

import com.pharmeasy.funnel.db.models.Segments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

public interface SegmentRepository extends JpaRepository<Segments, Long> {
    Segments findByParentSegmentIdAndType(String parentSegmentId, String Type);
}
