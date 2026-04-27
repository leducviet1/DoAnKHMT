package com.example.librarymanage_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SelfUpdateResponse {
    private UserResponse user;
    private String token;
    private String tokenType;
}
