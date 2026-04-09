package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.entity.BorrowDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowDetailRepository extends JpaRepository<BorrowDetail,Integer> {
    List<BorrowDetail> findByBorrow_BorrowId(Integer borrowId);
}
