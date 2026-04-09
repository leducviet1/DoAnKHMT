package com.example.librarymanage_be.entity;

import com.example.librarymanage_be.enums.BorrowDetailStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_details")
@Data
public class BorrowDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_id")
    private Borrow borrow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private Integer quantity;
    private String note;

    @Enumerated(EnumType.STRING)
    private BorrowDetailStatus status;

    private LocalDateTime returnDate;

}
