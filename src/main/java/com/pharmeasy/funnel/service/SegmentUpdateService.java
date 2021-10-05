package com.pharmeasy.funnel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.pharmeasy.funnel.db.models.EntityJobResult;
import com.pharmeasy.funnel.db.models.EntitySegments;
import com.pharmeasy.funnel.db.models.SegmentStore;
import com.pharmeasy.funnel.db.models.Segments;
import com.pharmeasy.funnel.db.repository.EntityRepository;
import com.pharmeasy.funnel.db.repository.SegmentRepository;
import com.pharmeasy.funnel.db.repository.SegmentStoreRepository;
import com.pharmeasy.funnel.exception.ExceptionUtils;
import com.pharmeasy.funnel.model.*;
import com.pharmeasy.funnel.utils.RedisQueueConsumer;
import com.pharmeasy.funnel.utils.RedisQueueProducer;
import com.pharmeasy.funnel.utils.RedisScripting;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class SegmentUpdateService {

    SegmentRepository segmentRepository;

    EntityRepository entityRepository;

    SegmentStoreRepository segmentStoreRepository;

    RedisQueueConsumer redisQueueConsumer;

    RedisQueueProducer redisQueueProducer;

    RedisScripting redisScripting;

    RedisTemplate redisTemplate;

    private ObjectMapper objectMapper;

    public UpdateResponse updateSegments() {
        try {
            updateSegmentDataInBulk();
        } catch (JsonProcessingException e) {
        }
        return UpdateResponse.builder().time(LocalDateTime.now()).status(Status.ACCEPTED).build();
    }

    public UpdateResponse updateSegment(UpdateRequest request) {
         updateSegmentBasedOnType(request.getSegmentId());
         redisScripting.rename(request.getSegmentId());
         return UpdateResponse.builder().time(LocalDateTime.now()).status(Status.ACCEPTED).build();
    }

    private List<String> updateSegmentDataInBulk() throws JsonProcessingException {
        List<String> segmentsUpdated = new ArrayList<>();
        Set<String> messageSet = redisQueueConsumer.readJobFromQueue();
        Iterator itr = messageSet.iterator();
            while (itr.hasNext()) {
                SegmentMessage message = objectMapper.readValue(itr.next().toString(), SegmentMessage.class);
                String segmentId = message.getId();
                segmentsUpdated.add(updateSegmentBasedOnType(segmentId));
            }
        return segmentsUpdated;
    }

    @Transactional
    private String updateSegmentBasedOnType(String segmentId) {
        Segments segment = validateSegment(segmentId);
        String segmentExists = (String)redisScripting.getSegmentType(segmentId);
        String newTransaction = UUID.randomUUID().toString();
        if (Boolean.parseBoolean(segmentExists)) {
            return updateOldSegment(segment, newTransaction);
        }
        return updateNewSegments(segment, newTransaction);
    }

    private String updateNewSegments(Segments segment, String newTransaction) {
        segmentStoreRepository.deleteBySegment(segment.getId());
        segmentStoreRepository.insert(segment, newTransaction);
        entityRepository.deleteBySegmentId(segment.getId());
        PageRequest pageable = PageRequest.of(0, 5000);
        Page<SegmentStore> pagedSegments = segmentStoreRepository.getSegmentDetailsByIdAndTransaction(segment.getId(), newTransaction, pageable);
        int totalPages = pagedSegments.getTotalPages();
        for ( int page = 0; page< totalPages; page++) {
            List<EntitySegments> entities = new ArrayList<>();
            List<String> entityIds = new ArrayList<>();
            int minValue = Math.min((page+1) * 5000, pagedSegments.getContent().size());
            List<SegmentStore> segments = pagedSegments.getContent().subList(page*5000, minValue);
            segments.forEach(segmentData -> {
                    entities.add(new EntitySegments("user",segmentData.getEntityID(), segmentData.getSegmentId()));
                    entityIds.add(segmentData.getEntityID());
            });
            entityRepository.saveAll(entities);
            if (segment.getStorage().equalsIgnoreCase("bitset")) {
            redisScripting.bitsetAdd(String.valueOf(segment.getId()), entityIds);
            }
            else {
                redisScripting.setAdd(String.valueOf(segment.getId()), entityIds);
            }
        }
        return String.valueOf(segment.getId());
    }

    private String updateOldSegment(Segments segment, String newTransaction) {
        SegmentStore segmentStore = segmentStoreRepository.getSegmentDetailsById(String.valueOf(segment.getId()), 1 );
        if ( segmentStore == null ) {
            return null;
        }
        String oldTransaction = segmentStore.getTransaction();
        segmentStoreRepository.insert(segment, newTransaction);
        updateSegmentsData(segment, newTransaction, oldTransaction);
        segmentStoreRepository.deleteBySegmentAndTransaction(segment.getId(), oldTransaction);
        return String.valueOf(segment.getId());
    }

    private void updateSegmentsData(Segments segment, String newTransaction, String oldTransaction) {
        List<EntityJobResult> deletedSegmentStoreList = segmentStoreRepository.deleteBulkOutdatedUsers( segment,  newTransaction,    oldTransaction);
        List<String> deletedEntityIds = deletedSegmentStoreList.stream().map(deletedSegmentStoreItem -> deletedSegmentStoreItem.getOldEntityId()).collect(Collectors.toList());
        //TODO: transactional
        //TODO : exception handling
        Lists.partition(deletedEntityIds, 5000).forEach(
                sublist -> {
                    List<EntitySegments> entities = new ArrayList<>();
                    sublist.forEach(entityId -> {
                        entities.add(new EntitySegments("user",entityId, (int)segment.getId()));
                    });
                    entityRepository.deleteAll(entities);
                    redisScripting.bitsetRemove( String.valueOf(segment.getId()),  sublist);
                }
        );
        List<EntityJobResult> updatedSegmentStoreList = segmentStoreRepository.addBulkNewUsers(segment,  newTransaction,    oldTransaction);
        List<String> updatedEntityIds = updatedSegmentStoreList.stream().map(updatedSegmentStoreItem -> updatedSegmentStoreItem.getNewEntityId()).collect(Collectors.toList());
        Lists.partition(updatedEntityIds, 5000).forEach(
                sublist -> {
                    List<EntitySegments> entities = new ArrayList<>();
                    sublist.forEach(entityId -> {
                        entities.add(new EntitySegments("user",entityId, (int)segment.getId()));
                    });
                    entityRepository.saveAll(entities);
                    redisScripting.bitsetAdd(String.valueOf(segment.getId()), sublist);
                }
        );
    }

    private Segments validateSegment(String segmentId) {
       Optional<Segments> segment =  segmentRepository.findById(Long.parseLong(segmentId));
       return segment.orElseThrow(ExceptionUtils.notFoundId(Segments.class, segmentId));
    }
}
