package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.LoginRequest;
import com.example.librarymanage_be.dto.request.RegisterRequest;
import com.example.librarymanage_be.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
