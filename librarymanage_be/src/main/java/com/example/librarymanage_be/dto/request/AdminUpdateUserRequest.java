package com.example.librarymanage_be.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class AdminUpdateUserRequest {
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private String password;
    private Set<String> roles;
}
