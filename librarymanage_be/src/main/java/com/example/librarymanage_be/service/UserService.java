package com.example.librarymanage_be.service;

import com.example.librarymanage_be.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService  {
User create(User user);
Page<User> getUsers(Pageable pageable);
User findById(Integer id);
User findByEmail(String email);
User update(User user);
void delete(Integer id);
}
