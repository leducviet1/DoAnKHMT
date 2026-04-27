package com.example.librarymanage_be.dto.request;

import lombok.Data;

@Data
public class SelfUpdateUserRequest {
    private String email;
    private String phoneNumber;
    private String password;
}
