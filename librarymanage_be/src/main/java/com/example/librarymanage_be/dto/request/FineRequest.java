package com.example.librarymanage_be.dto.request;

import com.example.librarymanage_be.enums.FineStatus;
import com.example.librarymanage_be.enums.FineType;
import lombok.Data;

@Data
public class FineRequest {
    private Integer borrowDetailId;
    private String reason;
    private FineType type;
    private FineStatus status;

}
