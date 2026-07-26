package com.careerconnect.dto;

import com.careerconnect.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String location;
    private String bio;
    private String skills;
    private LocalDateTime createdDate;
}
