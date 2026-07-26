package com.careerconnect.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String location;

    @Size(max = 1000)
    private String bio;

    @Size(max = 500)
    private String skills;
}
