package com.careerconnect.service;

import com.careerconnect.entity.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final URI RESEND_ENDPOINT = URI.create("https://api.resend.com/emails");

    private final ObjectMapper objectMapper;

    @Value("${resend.api-key:}")
    private String apiKey;

    @Value("${resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Async("mailExecutor")
    public void sendApplicationNotification(Application application) {
        String recruiterEmail = application.getJob().getRecruiter().getEmail();
        String applicantName = application.getApplicant().getName();
        String jobTitle = application.getJob().getTitle();

        if (!StringUtils.hasText(recruiterEmail)) {
            log.error("Recruiter email is empty for job id={}", application.getJob().getId());
            return;
        }
        send(
                recruiterEmail,
                "New Job Application - " + jobTitle,
                "Hello " + application.getJob().getRecruiter().getName() + ",\n\n"
                        + applicantName + " has applied for your job posting: " + jobTitle + ".\n\n"
                        + "Please log in to CareerConnect to review the application.\n\n"
                        + "Regards,\nCareerConnect"
        );
    }

    @Async("mailExecutor")
    public void sendApplicationConfirmation(Application application) {
        String applicantEmail = application.getApplicant().getEmail();
        String applicantName = application.getApplicant().getName();
        String jobTitle = application.getJob().getTitle();
        String companyName = application.getJob().getCompanyName();

        send(
                applicantEmail,
                "Application Received - " + jobTitle,
                "Hello " + applicantName + ",\n\n"
                        + "Thank you for applying for the " + jobTitle
                        + " position at " + companyName + ".\n\n"
                        + "We have successfully received your application. "
                        + "The recruiter will review your application and get in touch "
                        + "with you if your profile is shortlisted for the next stage.\n\n"
                        + "You can track your application status anytime by logging in "
                        + "to your CareerConnect account.\n\n"
                        + "Thank you for your interest in this opportunity.\n\n"
                        + "Regards,\n"
                        + "CareerConnect Team"
        );
    }

    @Async("mailExecutor")
    public void sendStatusUpdateNotification(Application application) {
        String applicantEmail = application.getApplicant().getEmail();
        String applicantName = application.getApplicant().getName();
        String jobTitle = application.getJob().getTitle();
        String companyName = application.getJob().getCompanyName();
        String status = application.getStatus().name();

        send(
                applicantEmail,
                "Application Status Update - " + jobTitle,
                "Hello " + applicantName + ",\n\n"
                        + "We wanted to let you know that there has been an update "
                        + "to your application for the " + jobTitle
                        + " position at " + companyName + ".\n\n"
                        + "Your current application status is: " + status + ".\n\n"
                        + "Please log in to your CareerConnect account to view the "
                        + "latest details regarding your application.\n\n"
                        + "Thank you for your interest and patience. "
                        + "We wish you the best in the next steps of the recruitment process.\n\n"
                        + "Regards,\n"
                        + "CareerConnect Team"
        );
    }

    private void send(String to, String subject, String text) {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("RESEND_API_KEY is not configured - skipping email to {}", to);
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "from", fromEmail,
                    "to", List.of(to),
                    "subject", subject,
                    "text", text
            );
            String body = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(RESEND_ENDPOINT)
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Email sent to {} - subject: {}", to, subject);
            } else {
                log.error("Resend API returned {} for recipient {}: {}",
                        response.statusCode(), to, response.body());
            }
        } catch (Exception ex) {
            log.error("Email notification failed for recipient {}: {}", to, ex.getMessage(), ex);
        }
    }
}
