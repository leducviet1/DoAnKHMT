package com.example.librarymanage_be.service;

import com.example.librarymanage_be.Entity.User;
import com.example.librarymanage_be.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        return null;
    }

    @Override
    public User findById(Integer id) {
        return userRepository.findById(id).orElseThrow(()->new RuntimeException("Not found user with id:"+id));
    }

    @Override
    public User findByEmail(String email) {
        return null;
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public void delete(Integer id) {

    }

}
