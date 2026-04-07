package com.example.librarymanage_be.dto.response;

import com.example.librarymanage_be.enums.FineStatus;
import com.example.librarymanage_be.enums.FineType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FineResponse {
    private Integer fineId;
    private Integer borrowDetailId;
    private BigDecimal amount;
    private String reason;
    private FineType type;
    private FineStatus status;
    private LocalDateTime createdAt;
}
