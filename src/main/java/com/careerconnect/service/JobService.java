package com.careerconnect.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.careerconnect.dto.JobRequest;
import com.careerconnect.dto.JobResponse;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.exception.ResourceNotFoundException;
import com.careerconnect.exception.UnauthorizedActionException;
import com.careerconnect.repository.ApplicationRepository;
import com.careerconnect.repository.JobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserService userService;

    public List<JobResponse> getAllJobs(String title, String location, String skills) {
        List<Job> jobs;
        if (StringUtils.hasText(title) || StringUtils.hasText(location) || StringUtils.hasText(skills)) {
            jobs = jobRepository.searchJobs(
                    StringUtils.hasText(title) ? title : null,
                    StringUtils.hasText(location) ? location : null,
                    StringUtils.hasText(skills) ? skills : null
            );
        } else {
            jobs = jobRepository.findAll();
        }
        return jobs.stream().map(this::toResponse).toList();
    }
    public Page<JobResponse> getJobsPage(String title, String location, String skills, Pageable pageable) {
        String normalizedTitle = StringUtils.hasText(title) ? title : null;
        String normalizedLocation = StringUtils.hasText(location) ? location : null;
        String normalizedSkills = StringUtils.hasText(skills) ? skills : null;

        return jobRepository.searchJobsPage(normalizedTitle, normalizedLocation, normalizedSkills, pageable)
                .map(this::toResponse);
    }

    public JobResponse getJobById(Long id) {
        Job job = findJobOrThrow(id);
        return toResponse(job);
    }

    public List<JobResponse> getJobsByRecruiter(String recruiterEmail) {
        User recruiter = userService.getUserByEmail(recruiterEmail);
        return jobRepository.findByRecruiterId(recruiter.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public JobResponse createJob(String recruiterEmail, JobRequest request) {
        User recruiter = userService.getUserByEmail(recruiterEmail);

        Job job = Job.builder()
                .title(request.getTitle())
                .companyName(request.getCompanyName())
                .location(request.getLocation())
                .salary(request.getSalary())
                .experience(request.getExperience())
                .skills(request.getSkills())
                .description(request.getDescription())
                .recruiter(recruiter)
                .build();

        return toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse updateJob(String recruiterEmail, Long jobId, JobRequest request) {
        Job job = findJobOrThrow(jobId);
        ensureOwnership(job, recruiterEmail);

        job.setTitle(request.getTitle());
        job.setCompanyName(request.getCompanyName());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setExperience(request.getExperience());
        job.setSkills(request.getSkills());
        job.setDescription(request.getDescription());

        return toResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(String recruiterEmail, Long jobId) {
        Job job = findJobOrThrow(jobId);
        ensureOwnership(job, recruiterEmail);
        jobRepository.delete(job);
    }

    private void ensureOwnership(Job job, String recruiterEmail) {
        if (!job.getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new UnauthorizedActionException("You are not authorized to modify this job posting");
        }
    }

    private Job findJobOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompanyName())
                .location(job.getLocation())
                .salary(job.getSalary())
                .experience(job.getExperience())
                .skills(job.getSkills())
                .description(job.getDescription())
                .createdDate(job.getCreatedDate())
                .recruiterId(job.getRecruiter().getId())
                .recruiterName(job.getRecruiter().getName())
                .applicantCount(applicationRepository.countByJob(job))
                .build();
    }
}
