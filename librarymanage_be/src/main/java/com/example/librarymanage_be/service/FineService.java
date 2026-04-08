package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.FineRequest;
import com.example.librarymanage_be.dto.response.FineResponse;
import com.example.librarymanage_be.enums.FineType;
import com.example.librarymanage_be.Entity.BorrowDetail;
import com.example.librarymanage_be.Entity.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface FineService {
    FineResponse create(FineRequest fineRequest);
    Page<FineResponse> getFines(Pageable pageable);
    BigDecimal calculateAmount(FineType fineType, BorrowDetail borrowDetail);
    void pay(Integer fineId);
    Fine findById(Integer fineId);
}
