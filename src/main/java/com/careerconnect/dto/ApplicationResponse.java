package com.careerconnect.dto;

import com.careerconnect.entity.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private ApplicationStatus status;
    private LocalDateTime appliedDate;
}
