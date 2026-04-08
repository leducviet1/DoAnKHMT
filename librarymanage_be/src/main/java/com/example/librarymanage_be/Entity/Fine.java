package com.example.librarymanage_be.Entity;

import com.example.librarymanage_be.enums.FineStatus;
import com.example.librarymanage_be.enums.FineType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "fines")
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fineId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "borrow_detail_id",unique = true)
    private BorrowDetail borrowDetail;

    private BigDecimal amount;
    private String reason;

    @Enumerated(EnumType.STRING)
    private FineType type;

    @Enumerated(EnumType.STRING)
    private FineStatus status;

    private LocalDateTime createdAt;
}
