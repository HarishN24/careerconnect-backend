package com.careerconnect.service;

import com.careerconnect.dto.ApplicationResponse;
import com.careerconnect.entity.Application;
import com.careerconnect.entity.ApplicationStatus;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.exception.DuplicateResourceException;
import com.careerconnect.exception.ResourceNotFoundException;
import com.careerconnect.exception.UnauthorizedActionException;
import com.careerconnect.repository.ApplicationRepository;
import com.careerconnect.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public ApplicationResponse applyToJob(String applicantEmail, Long jobId) {
        User applicant = userService.getUserByEmail(applicantEmail);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (applicationRepository.existsByJobAndApplicant(job, applicant)) {
            throw new DuplicateResourceException("You have already applied to this job");
        }

        Application application = Application.builder()
                .job(job)
                .applicant(applicant)
                .status(ApplicationStatus.APPLIED)
                .build();
        Application saved = applicationRepository.save(application);
        notificationService.sendApplicationNotification(saved);
        notificationService.sendApplicationConfirmation(saved);
        return toResponse(saved);
    }

    public List<ApplicationResponse> getMyApplications(String applicantEmail) {
        User applicant = userService.getUserByEmail(applicantEmail);
        return applicationRepository.findByApplicant(applicant)
                .stream().map(this::toResponse).toList();
    }

    public List<ApplicationResponse> getApplicationsForJob(String recruiterEmail, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new UnauthorizedActionException("You are not authorized to view applicants for this job");
        }

        return applicationRepository.findByJob(job)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ApplicationResponse updateStatus(String recruiterEmail, Long applicationId, ApplicationStatus status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!application.getJob().getRecruiter().getEmail().equalsIgnoreCase(recruiterEmail)) {
            throw new UnauthorizedActionException("You are not authorized to update this application");
        }

        application.setStatus(status);
        Application saved= applicationRepository.save(application);
        notificationService.sendStatusUpdateNotification(saved);
        return toResponse(saved);
    }

    private ApplicationResponse toResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .companyName(application.getJob().getCompanyName())
                .applicantId(application.getApplicant().getId())
                .applicantName(application.getApplicant().getName())
                .applicantEmail(application.getApplicant().getEmail())
                .status(application.getStatus())
                .appliedDate(application.getAppliedDate())
                .build();
    }
}
