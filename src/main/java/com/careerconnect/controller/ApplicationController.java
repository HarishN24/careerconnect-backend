package com.careerconnect.controller;

import com.careerconnect.dto.ApplicationResponse;
import com.careerconnect.dto.ApplicationStatusUpdateRequest;
import com.careerconnect.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/{jobId}")
    public ResponseEntity<ApplicationResponse> applyToJob(Authentication authentication,
                                                            @PathVariable Long jobId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.applyToJob(authentication.getName(), jobId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getMyApplications(authentication.getName()));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForJob(Authentication authentication,
                                                                             @PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsForJob(authentication.getName(), jobId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(Authentication authentication,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(authentication.getName(), id, request.getStatus()));
    }
}
