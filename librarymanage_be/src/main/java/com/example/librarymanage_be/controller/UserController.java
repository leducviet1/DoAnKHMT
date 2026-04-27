package com.example.librarymanage_be.controller;

import com.example.librarymanage_be.dto.request.AdminUpdateUserRequest;
import com.example.librarymanage_be.dto.request.SelfUpdateUserRequest;
import com.example.librarymanage_be.dto.response.SelfUpdateResponse;
import com.example.librarymanage_be.dto.response.UserResponse;
import com.example.librarymanage_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Page<UserResponse> getUsers(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.getUsers(pageable);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Integer id) {
        return userService.getUserResponseById(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Integer id, @RequestBody AdminUpdateUserRequest request) {
        return userService.updateByAdmin(id, request);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal(expression = "username") String email) {
        return userService.getCurrentUser(email);
    }

    @PutMapping("/me")
    public SelfUpdateResponse updateCurrentUser(@AuthenticationPrincipal(expression = "username") String email,
                                                @RequestBody SelfUpdateUserRequest request) {
        return userService.updateCurrentUser(email, request);
    }
}
