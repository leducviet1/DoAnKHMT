package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.Entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRepository extends JpaRepository<Borrow,Integer> {
}
