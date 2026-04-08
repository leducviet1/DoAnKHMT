package com.example.librarymanage_be.service;

import com.example.librarymanage_be.config.FineConfig;
import com.example.librarymanage_be.dto.request.FineRequest;
import com.example.librarymanage_be.dto.response.FineResponse;
import com.example.librarymanage_be.enums.FineStatus;
import com.example.librarymanage_be.enums.FineType;
import com.example.librarymanage_be.mapper.FineMapper;
import com.example.librarymanage_be.Entity.BorrowDetail;
import com.example.librarymanage_be.Entity.Fine;
import com.example.librarymanage_be.repo.FineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {
    private final BorrowDetailService borrowDetailService;
    private final FineMapper fineMapper;
    private final FineRepository fineRepository;

    @Override
    public FineResponse create(FineRequest fineRequest) {
        log.info("[FINE] Created fine with id={}",fineRequest.getBorrowDetailId());
        BorrowDetail borrowDetail = borrowDetailService.findById(fineRequest.getBorrowDetailId());
        Fine fine = fineMapper.toEntity(fineRequest);
        fine.setBorrowDetail(borrowDetail);
        fine.setStatus(FineStatus.PENDING);
        BigDecimal amount = calculateAmount(fineRequest.getType(), borrowDetail);
        fine.setAmount(amount);
        fineRepository.save(fine);
        log.info("[FINE] Created fine with id={}",fineRequest.getBorrowDetailId());
        return fineMapper.toResponse(fine);
    }

    @Override
    public Page<FineResponse> getFines(Pageable pageable) {
        log.info("[FINE] Getting fines for page={},size={}",pageable.getPageNumber(),pageable.getPageSize());
        Page<Fine> fines = fineRepository.findAll(pageable);
        log.info("[FINE] Found {} fines",fines.getTotalElements());
        return fines.map(fineMapper::toResponse);
    }

    @Override
    public BigDecimal calculateAmount(FineType fineType, BorrowDetail borrowDetail) {
        if (fineType == FineType.LOST) {
            return borrowDetail.getBook().getPrice().multiply(FineConfig.LOST_PERCENT);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public void pay(Integer fineId) {
        log.info("[FINE] Starting payment for fine with id={}",fineId);
        Fine fine = findById(fineId);
        fine.setStatus(FineStatus.PAIDED);
        fineRepository.save(fine);
        log.info("[FINE] Payment successfully for fine with id={}",fineId);
    }

    @Override
    public Fine findById(Integer fineId) {
        log.info("[FINE] Getting fine with id={}",fineId);
        return fineRepository.findById(fineId).orElseThrow(()->
        {
            log.info("[FINE] Not found fine with id={}",fineId);
            return new RuntimeException("Not found");
        });
    }

}
