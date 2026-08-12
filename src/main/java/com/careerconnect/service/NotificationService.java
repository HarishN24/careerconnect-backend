package com.careerconnect.service;

import com.careerconnect.entity.Application;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;


    @Async("mailExecutor")
    public void sendApplicationNotification(Application application) {
        if (!StringUtils.hasText(mailFrom)) {
            log.warn("spring.mail.username is not configured - skipping recruiter notification email");
            return;
        }

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
        if (!StringUtils.hasText(mailFrom)) {
            log.warn("spring.mail.username is not configured - skipping applicant confirmation email");
            return;
        }

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
        if (!StringUtils.hasText(mailFrom)) {
            log.warn("spring.mail.username is not configured - skipping status update email");
            return;
        }

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
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent to {} - subject: {}", to, subject);
        } catch (MailException ex) {

            log.error("Email notification failed for recipient {}: {}", to, ex.getMessage(), ex);
        }
    }
}
