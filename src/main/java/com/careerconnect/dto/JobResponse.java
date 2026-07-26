package com.careerconnect.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String companyName;
    private String location;
    private String salary;
    private String experience;
    private String skills;
    private String description;
    private LocalDateTime createdDate;
    private Long recruiterId;
    private String recruiterName;
    private long applicantCount;
}
