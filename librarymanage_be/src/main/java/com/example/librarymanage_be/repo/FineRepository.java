package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.entity.Fine;
import com.example.librarymanage_be.enums.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Integer>, PagingAndSortingRepository<Fine, Integer> {
    Page<Fine> findByBorrowDetail_Borrow_User_Email(String email, Pageable pageable);

    long countByStatus(FineStatus status);

    @Query("select coalesce(sum(f.amount), 0) from Fine f where f.status = :status")
    BigDecimal sumAmountByStatus(FineStatus status);

    List<Fine> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
