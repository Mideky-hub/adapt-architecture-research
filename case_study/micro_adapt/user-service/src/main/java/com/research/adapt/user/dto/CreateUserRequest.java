package com.research.adapt.user.dto;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
}
