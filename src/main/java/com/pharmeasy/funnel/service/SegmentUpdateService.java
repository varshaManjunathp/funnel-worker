package com.pharmeasy.funnel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.pharmeasy.funnel.db.models.EntitySegments;
import com.pharmeasy.funnel.db.models.SegmentStore;
import com.pharmeasy.funnel.db.models.Segments;
import com.pharmeasy.funnel.db.repository.EntityRepository;
import com.pharmeasy.funnel.db.repository.SegmentRepository;
import com.pharmeasy.funnel.db.repository.SegmentStoreRepository;
import com.pharmeasy.funnel.exception.ExceptionUtils;
import com.pharmeasy.funnel.model.*;
import com.pharmeasy.funnel.utils.RedisQueueConsumer;
import com.pharmeasy.funnel.utils.RedisScripting;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SegmentUpdateService {

    SegmentRepository segmentRepository;

    EntityRepository entityRepository;

    SegmentStoreRepository segmentStoreRepository;

    RedisQueueConsumer redisQueueConsumer;

    RedisScripting redisScripting;

    RedisTemplate redisTemplate;

    private ObjectMapper objectMapper;

    public UpdateResponse updateSegments() {
        return null;
    }

    public UpdateResponse updateSegment(UpdateRequest request) {
         getSegmentData(request.getSegmentId());
         return UpdateResponse.builder().time(LocalDateTime.now()).status(Status.ACCEPTED).build();
    }

    @Async
    public void getSegmentData(String segmentId) {
        try {
            List<String> segmentsToBeUpdated = updateSegmentData();
        } catch (JsonProcessingException e) {
        }
    }

    private List<String> updateSegmentData() throws JsonProcessingException {
        List<String> segmentsUpdated = new ArrayList<>();
        Set<String> messageSet = redisQueueConsumer.readJobFromQueue();
        Iterator itr = messageSet.iterator();
            while (itr.hasNext()) {
                SegmentMessage message = objectMapper.readValue(itr.next().toString(), SegmentMessage.class);

                String segmentId = message.getId();
                Segments segment = validateSegment(segmentId);
                String segmentType = redisScripting.getSegmentType();
                String newTransaction = UUID.randomUUID().toString();
                if (segmentType.equalsIgnoreCase("none")) {
                    String updatedId =  updateOldSegment(segment, newTransaction);
                    if (updatedId != null) {
                        segmentsUpdated.add(updatedId);
                        continue;
                    }
                } 
                 segmentsUpdated.add(updateNewSegments(segment, newTransaction));
            }
        return null;
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
            List<SegmentStore> segments = pagedSegments.getContent().subList(page*5000, (page+1)*5000);
            segments.forEach(segmentData -> {
                    entities.add(new EntitySegments("user",segmentData.getEntityID(), segmentData.getSegmentId()));
                    entityIds.add(segmentData.getEntityID());
            });
            entityRepository.saveAll(entities);
            if (segment.getStorage().equalsIgnoreCase("bitset")) {
            redisScripting.bitsetAdd(entityIds);
            }
            else {
                redisScripting.setAdd( entityIds);
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
        List<SegmentStore> deletedSegmentStoreList = segmentStoreRepository.deleteBulkOutdatedUsers( segment,  newTransaction,    oldTransaction);
        List<String> deletedEntityIds = deletedSegmentStoreList.stream().map(deletedSegmentStoreItem -> deletedSegmentStoreItem.getEntityID()).collect(Collectors.toList());
        Lists.partition(deletedEntityIds, 100).forEach(
                sublist -> redisScripting.bitsetRemove(sublist)
        );
        List<SegmentStore> updatedSegmentStoreList =segmentStoreRepository.addBulkNewUsers(segment,  newTransaction,    oldTransaction);
        List<String> updatedEntityIds = updatedSegmentStoreList.stream().map(updatedSegmentStoreItem -> updatedSegmentStoreItem.getEntityID()).collect(Collectors.toList());
        Lists.partition(updatedEntityIds, 5000).forEach(
                sublist -> redisScripting.bitsetAdd(sublist)
        );
    }

    private Segments validateSegment(String segmentId) {
       Optional<Segments> segment =  segmentRepository.findById(Long.parseLong(segmentId));
       return segment.orElseThrow(ExceptionUtils.notFoundId(Segments.class, segmentId));
    }
}
