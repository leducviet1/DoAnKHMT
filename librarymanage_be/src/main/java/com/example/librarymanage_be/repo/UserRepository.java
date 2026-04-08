package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User,Integer> , PagingAndSortingRepository<User,Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
