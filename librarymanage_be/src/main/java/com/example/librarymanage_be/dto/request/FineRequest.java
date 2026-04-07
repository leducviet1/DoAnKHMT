package com.example.librarymanage_be.dto.request;

import com.example.librarymanage_be.enums.FineStatus;
import com.example.librarymanage_be.enums.FineType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FineRequest {
    private Integer borrowDetailId;
    private String reason;
    private FineType type;
    private FineStatus status;

}
