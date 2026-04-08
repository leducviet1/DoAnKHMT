package com.example.librarymanage_be.controller;

import com.example.librarymanage_be.Entity.User;
import com.example.librarymanage_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping
    public User addUser(@RequestBody User user) {
        return userService.create(user);
    }
}
