package com.quizapp.controller;

import com.quizapp.dto.ReattemptResolutionRequest;
import com.quizapp.entity.ReattemptRequestEntity;
import com.quizapp.service.ReattemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reattempts")
public class ReattemptRequestController {

    private final ReattemptService reattemptService;

    @Autowired
    public ReattemptRequestController(ReattemptService reattemptService) {
        this.reattemptService = reattemptService;
    }

    @GetMapping
    public ResponseEntity<List<ReattemptRequestEntity>> getAllRequests(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String teacherId) {
        return ResponseEntity.ok(reattemptService.getAllRequests(studentId, teacherId));
    }

    @PostMapping
    public ResponseEntity<ReattemptRequestEntity> submitRequest(@RequestBody ReattemptRequestEntity request) {
        return ResponseEntity.ok(reattemptService.submitRequest(request));
    }

    @PatchMapping("/{requestId}/resolve")
    public ResponseEntity<ReattemptRequestEntity> resolveRequest(
            @PathVariable String requestId,
            @RequestBody ReattemptResolutionRequest request) {
        return ResponseEntity.ok(reattemptService.resolveRequest(requestId, request.getStatus(), request.getPaperMode()));
    }
}
