package com.pharmeasy.funnel.db.repository;

import com.pharmeasy.funnel.db.models.EntityJobResult;
import com.pharmeasy.funnel.db.models.SegmentStore;
import com.pharmeasy.funnel.db.models.Segments;
import com.pharmeasy.funnel.model.SegmentStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

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

    private void setHiveUpdateProperties() {
        jdbcTemplate.execute("SET hive.txn.manager=org.apache.hadoop.hive.ql.lockmgr.DbTxnManager");
        jdbcTemplate.execute("SET hive.support.concurrency=true");
        jdbcTemplate.execute("SET hive.enforce.bucketing=true");
        jdbcTemplate.execute("SET hive.exec.dynamic.partition.mode=nonstrict");
    }

    public SegmentStore getSegmentDetailsById(String segmentId, int limit) {
        setHiveUpdateProperties();
        String SQL = "SELECT * FROM segment_store_"+ environment +" WHERE segment_id = "+ segmentId +" LIMIT "+ limit;
        return jdbcTemplate.queryForObject(SQL, new SegmentStoreMapper());
    }

    public void insert(Segments segment, String transaction) {
        String sql = String.format("INSERT INTO %s (entity_id, entity, source, segment_id, transaction)\n" +
                "\t\t(SELECT \n" +
                "\t\t\tres.user_id as entity_id, 'user' as entity, 'skull' as source, %d as segment_id, '%s' as transaction \n" +
                "\t\tFROM (\n" +
                "\t\t\t%s\n" +
                "\t\t) as res)", getTableName(environment), segment.getId(), transaction, segment.getQuery());
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

    public void deleteBySegment(long segmentId) {
        setHiveUpdateProperties();
        String SQL = String.format("DELETE from %s where segment_id = \"?\"", getTableName(environment) );
        jdbcTemplate.update(SQL, segmentId);
    }

    public List<EntityJobResult> deleteBulkOutdatedUsers(Segments segment, String newTransaction, String oldTransaction) {
        String sql = String.format(
                " WITH base AS ( SELECT  * from %s where segment_id = %d AND transaction = '%s') " +
                " SELECT " +
                " a.entity_id as old_entity_id , b.entity_id as new_entity_id " +
                " FROM %s a " +
                " LEFT JOIN " +
                " base as b " +
                " ON " +
                " a.entity_id = b.entity_id " +
                " WHERE " +
                " a.segment_id = %d AND a.transaction = '%s' ",  getTableName(environment), segment.getId(), newTransaction, getTableName(environment), segment.getId(), oldTransaction );
        List<EntityJobResult> entityJobResults = jdbcTemplate.query(sql, new EntityMapper());
        return entityJobResults.stream().filter(entity -> entity.getNewEntityId()==null).collect(Collectors.toList());
    }

    public List<EntityJobResult> addBulkNewUsers(Segments segment, String newTransaction, String oldTransaction) {
        String sql = String.format(
                " WITH base AS ( SELECT  * from %s where segment_id = %d AND transaction = '%s') " +
                " SELECT " +
                " a.entity_id as old_entity_id, b.entity_id as new_entity_id " +
                " FROM %s a " +
                " RIGHT JOIN " +
                " base as b " +
                " ON " +
                " a.entity_id = b.entity_id " +
                " AND " +
                " a.segment_id = %d AND a.transaction = '%s' " +
                "",  getTableName(environment), segment.getId(), newTransaction, getTableName(environment), segment.getId(), oldTransaction );
        List<EntityJobResult> entityJobResults = jdbcTemplate.query(sql, new EntityMapper());
        return entityJobResults.stream().filter(entity -> entity.getOldEntityId()==null).collect(Collectors.toList());
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

     class EntityMapper implements RowMapper<EntityJobResult> {

         @Override
         public EntityJobResult mapRow(ResultSet rs, int rowNum) throws SQLException {
             EntityJobResult entityJobResult = new EntityJobResult();
             entityJobResult.setOldEntityId(rs.getString("old_entity_id"));
             entityJobResult.setNewEntityId(rs.getString("new_entity_id"));

             return entityJobResult;
         }
     }
