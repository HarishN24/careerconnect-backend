package com.careerconnect.controller;

import com.careerconnect.dto.JobRequest;
import com.careerconnect.dto.JobResponse;
import com.careerconnect.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skills) {
        return ResponseEntity.ok(jobService.getAllJobs(title, location, skills));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @GetMapping("/my-postings")
    public ResponseEntity<List<JobResponse>> getMyPostings(Authentication authentication) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(Authentication authentication,
                                                  @Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.createJob(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(Authentication authentication, @PathVariable Long id) {
        jobService.deleteJob(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
