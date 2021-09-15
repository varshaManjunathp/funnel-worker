package com.pharmeasy.funnel.controller;

import com.pharmeasy.funnel.model.UpdateRequest;
import com.pharmeasy.funnel.model.UpdateResponse;
import com.pharmeasy.funnel.service.SegmentUpdateService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/funnel-worker", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class SegmentUpdateController {

    SegmentUpdateService segmentUpdateService;

    @PostMapping(path = "/bulkUpdate")
    public UpdateResponse updateSegments() {
       return segmentUpdateService.updateSegments();
    }

    @PostMapping(path = "/update")
    public UpdateResponse updateSegment(@RequestBody UpdateRequest request) {
        return segmentUpdateService.updateSegment(request);
    }
}
