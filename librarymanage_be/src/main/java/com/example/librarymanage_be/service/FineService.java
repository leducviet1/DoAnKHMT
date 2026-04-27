package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.FineRequest;
import com.example.librarymanage_be.dto.response.FineResponse;
import com.example.librarymanage_be.enums.FineType;
import com.example.librarymanage_be.entity.BorrowDetail;
import com.example.librarymanage_be.entity.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface FineService {
    FineResponse create(FineRequest fineRequest);

    Page<FineResponse> getFines(Pageable pageable);

    Page<FineResponse> getFines(Pageable pageable, String email, boolean canViewAll);

    BigDecimal calculateAmount(FineType fineType, BorrowDetail borrowDetail);

    void pay(Integer fineId);

    Fine findById(Integer fineId);
}
