package com.example.librarymanage_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserResponse {
    private Integer userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDateTime createdAt;
    private Set<String> roles;
}
