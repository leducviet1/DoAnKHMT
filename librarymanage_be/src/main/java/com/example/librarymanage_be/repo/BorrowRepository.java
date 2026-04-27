package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.entity.Borrow;
import com.example.librarymanage_be.enums.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowRepository extends JpaRepository<Borrow,Integer>, PagingAndSortingRepository<Borrow,Integer> {
    long countByStatus(BorrowStatus status);

    List<Borrow> findAllByBorrowDateBetween(LocalDateTime start, LocalDateTime end);

    Page<Borrow> findByUser_Email(String email, Pageable pageable);
}
