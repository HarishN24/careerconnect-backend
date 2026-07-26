package com.careerconnect.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {
    // jobId comes from the path variable; body reserved for future fields
    // such as a cover letter or resume link.
    private String coverLetter;
}
