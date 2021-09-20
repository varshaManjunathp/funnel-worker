package com.pharmeasy.funnel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmeasy.funnel.db.models.SegmentStore;
import com.pharmeasy.funnel.db.models.Segments;
import com.pharmeasy.funnel.db.repository.SegmentRepository;
import com.pharmeasy.funnel.db.repository.SegmentStoreRepository;
import com.pharmeasy.funnel.exception.ExceptionUtils;
import com.pharmeasy.funnel.model.*;
import com.pharmeasy.funnel.utils.RedisQueueConsumer;
import com.pharmeasy.funnel.utils.RedisScripting;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class SegmentUpdateService {

    SegmentRepository segmentRepository;

    SegmentStoreRepository segmentStoreRepository;

    RedisQueueConsumer redisQueueConsumer;

    RedisScripting redisScripting;

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
                if (segmentType.equalsIgnoreCase("none")) {
                    String updatedId =  updateOldSegment(segment);
                    if (updatedId != null) {
                        segmentsUpdated.add(updatedId);
                        continue;
                    }
                } 
                 segmentsUpdated.add(updateNewSegments());
            }
        return null;
    }

    private String updateNewSegments() {
        return null;
    }

    private String updateOldSegment(Segments segment) {
        String newTransaction = UUID.randomUUID().toString();
        SegmentStore segmentStore = segmentStoreRepository.getSegmentDetailsById(String.valueOf(segment.getId()), 1 );
        if ( segmentStore == null ) {
            return null;
        }
        String oldTransaction = segmentStore.getTransaction();
        segmentStoreRepository.insert(segment, newTransaction);
        updateSegmentsData(segment, newTransaction, oldTransaction);
        return null;
    }

    private void updateSegmentsData(Segments segment, String newTransaction, String oldTransaction) {
        segmentStoreRepository.deleteBulkOutdatedUsers( segment,  newTransaction,    oldTransaction);

        segmentStoreRepository.addBulkNewUsers(segment,  newTransaction,    oldTransaction);
    }

    private Segments validateSegment(String segmentId) {
       Optional<Segments> segment =  segmentRepository.findById(Long.parseLong(segmentId));
       return segment.orElseThrow(ExceptionUtils.notFoundId(Segments.class, segmentId));
    }
}
