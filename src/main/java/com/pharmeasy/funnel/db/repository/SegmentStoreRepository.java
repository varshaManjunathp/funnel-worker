package com.pharmeasy.funnel.db.repository;

import com.pharmeasy.funnel.db.models.SegmentStore;
import com.pharmeasy.funnel.db.models.Segments;
import com.pharmeasy.funnel.model.SegmentStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.*;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class SegmentStoreRepository  {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    SegmentRepository segmentRepository;

    @Value("${env}")
    String environment;

    public SegmentStore getSegmentDetailsById(String segmentId, int limit) {
        String SQL = "SELECT * FROM segment_store_"+ environment +" WHERE segment_id = "+ segmentId +" LIMIT "+ limit;
        return jdbcTemplate.queryForObject(SQL, new SegmentStoreMapper());
    }

    public void insert(Segments segment, String transaction) {
        String sql = String.format("INSERT INTO %s (entity_id, entity, source, segment_id, transaction)\n" +
                "\t\tSELECT \n" +
                "\t\t\tres.user_id as entity_id, 'user' as entity, 'skull' as source, %d as segment_id, '%s' as transaction \n" +
                "\t\tFROM (\n" +
                "\t\t\t%s\n" +
                "\t\t) as res", getTableName(environment), segment.getId(), transaction, segment.getQuery());
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            segment.setStatus(SegmentStatus.FAILED);
            segmentRepository.save(segment);
            Segments conSegment = segmentRepository
                    .findByParentSegmentIdAndType(segment.getParentSegmentId(), "con");
            if (conSegment != null ) {
                conSegment.setStatus(SegmentStatus.FAILED);
                segmentRepository.save(conSegment);
            }
        }
    }

    public Page<SegmentStore> getSegmentDetailsByIdAndTransaction(long segmentId, String transaction, Pageable pageRequest) {
      String countSQL = String.format("SELECT count(*) FROM %s WHERE segment_id = %d and transaction = \"%s\" ", getTableName(environment), segmentId, transaction);
      int count = jdbcTemplate.queryForObject(countSQL, Integer.class);

        String SQL = String.format("SELECT * FROM %s WHERE segment_id = %d and transaction = \"%s\" ORDER BY entity_id ASC LIMIT %d OFFSET %d", getTableName(environment), segmentId, transaction,
                pageRequest.getPageSize(), pageRequest.getOffset());
        List<SegmentStore> segmentStores =  jdbcTemplate.query(SQL, new SegmentStoreMapper());
        return new PageImpl<>(segmentStores, pageRequest, count);
    }

    public void deleteBySegmentAndTransaction(long segmentId, String transaction) {
        String SQL = String.format("DELETE from %s where segment_id = ? and transaction = ? ", getTableName(environment) );
        jdbcTemplate.update(SQL, segmentId, transaction);
    }

    @Transactional
    public void deleteBySegment(long segmentId) {
        String SQL = String.format("DELETE from %s where segment_id = ? ", getTableName(environment) );
        jdbcTemplate.update(SQL, segmentId);
    }

    public List<SegmentStore> deleteBulkOutdatedUsers(Segments segment, String newTransaction, String oldTransaction) {
        String sql = String.format("SELECT * from (\n" +
                "\t\tWITH base AS ( SELECT  * from %s where segment_id = %d AND transaction = '%s')\n" +
                "\t\tSELECT\n" +
                "\t\t\ta.entity_id as old_entity_id , b.entity_id as new_entity_id\n" +
                "\t\tFROM %s a\n" +
                "\t\t\tLEFT JOIN\n" +
                "\t\tbase as b\n" +
                "\t\t\tON\n" +
                "\t\ta.entity_id = b.entity_id\n" +
                "\t\t\tWHERE\n" +
                "\t\ta.segment_id = %d AND a.transaction = '%s'\n" +
                "\t) WHERE new_entity_id is NULL",  getTableName(environment), segment.getId(), newTransaction, getTableName(environment), segment.getId(), oldTransaction );
        List<SegmentStore> segmentStoreList = jdbcTemplate.query(sql, new SegmentStoreMapper());
        List<Object[]> batchArgs = new ArrayList<>();
        for (SegmentStore segmentStore : segmentStoreList) {
            batchArgs.add(new Object[] { segmentStore.getSegmentId() });
        }
        String deleteSQL = String.format("DELETE from %s where segment_id = ?", getTableName(environment) );
        jdbcTemplate.batchUpdate(deleteSQL, batchArgs);
        return segmentStoreList;
    }

    public List<SegmentStore> addBulkNewUsers(Segments segment, String newTransaction, String oldTransaction) {
        String sql = String.format("SELECT * from (\n" +
                "\t\tWITH base AS ( SELECT  * from %s where segment_id = %d AND transaction = '%s')\n" +
                "\t\tSELECT\n" +
                "\t\t\ta.entity_id as old_entity_id, b.entity_id as new_entity_id\n" +
                "\t\tFROM %s a\n" +
                "\t\t\tRIGHT JOIN\n" +
                "\t\tbase as b\n" +
                "\t\t\tON\n" +
                "\t\ta.entity_id = b.entity_id\n" +
                "\t\t\tAND\n" +
                "\t\ta.segment_id = %d AND a.transaction = '%s'\n" +
                "\t) WHERE old_entity_id is NULL",  getTableName(environment), segment.getId(), newTransaction, getTableName(environment), segment.getId(), oldTransaction );
        List<SegmentStore> segmentStoreList = jdbcTemplate.query(sql, new SegmentStoreMapper());
        List<Object[]> batchArgs = new ArrayList<>();
        for (SegmentStore segmentStore : segmentStoreList) {
            batchArgs.add(new Object[] { segmentStore.getSegmentId(), segmentStore.getEntity(), segmentStore.getEntityID(), segmentStore.getSource(), segmentStore.getTransaction() });
        }
        String deleteSQL = String.format("INSERT into %s ( segment_id, entity, entity_id, source, transaction ) values (?, ?, ? , ?, ?)", getTableName(environment) );
        jdbcTemplate.batchUpdate(deleteSQL, batchArgs);

        return segmentStoreList;
    }


    public String getTableName(String environment) {
        return "segment_store_"+environment;
    }
}

 class SegmentStoreMapper implements RowMapper<SegmentStore> {
    @Override
    public SegmentStore mapRow(ResultSet rs, int rowNum) throws SQLException {
                 SegmentStore segmentStore = new SegmentStore();
                 segmentStore.setSegmentId(rs.getInt("segment_id"));
                 segmentStore.setEntity(rs.getString("entity"));
                 segmentStore.setEntityID(rs.getString("entity_id"));
                 segmentStore.setTransaction(rs.getString("transaction"));
                 segmentStore.setSource(rs.getString("source"));
    return segmentStore;
    }
}