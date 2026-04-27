package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.AdminUpdateUserRequest;
import com.example.librarymanage_be.dto.request.SelfUpdateUserRequest;
import com.example.librarymanage_be.dto.response.SelfUpdateResponse;
import com.example.librarymanage_be.dto.response.UserResponse;
import com.example.librarymanage_be.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService {
    User create(User user);

    Page<UserResponse> getUsers(Pageable pageable);

    User findById(Integer id);

    UserResponse getUserResponseById(Integer id);

    User findByEmail(String email);

    User update(User user);

    void delete(Integer id);

    UserResponse getCurrentUser(String email);

    SelfUpdateResponse updateCurrentUser(String currentEmail, SelfUpdateUserRequest request);

    UserResponse updateByAdmin(Integer id, AdminUpdateUserRequest request);
}
