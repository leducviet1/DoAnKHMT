package com.example.librarymanage_be.service;

import com.example.librarymanage_be.config.FineConfig;
import com.example.librarymanage_be.dto.request.FineRequest;
import com.example.librarymanage_be.dto.response.FineResponse;
import com.example.librarymanage_be.enums.FineStatus;
import com.example.librarymanage_be.enums.FineType;
import com.example.librarymanage_be.mapper.FineMapper;
import com.example.librarymanage_be.model.BorrowDetail;
import com.example.librarymanage_be.model.Fine;
import com.example.librarymanage_be.repo.FineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {
    private final BorrowDetailService borrowDetailService;
    private final FineMapper fineMapper;
    private final FineRepository fineRepository;

    @Override
    public FineResponse create(FineRequest fineRequest) {
        BorrowDetail borrowDetail = borrowDetailService.findById(fineRequest.getBorrowDetailId());
        Fine fine = fineMapper.toEntity(fineRequest);
        fine.setBorrowDetail(borrowDetail);
        fine.setStatus(FineStatus.PENDING);

        BigDecimal amount = calculateAmount(fineRequest.getType(), borrowDetail);
        fine.setAmount(amount);
        fineRepository.save(fine);
        return fineMapper.toResponse(fine);
    }

    @Override
    public Page<FineResponse> getFines(Pageable pageable) {
        Page<Fine> fines = fineRepository.findAll(pageable);
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
        Fine fine = findById(fineId);
        fine.setStatus(FineStatus.PAIDED);
        fineRepository.save(fine);
    }

    @Override
    public Fine findById(Integer fineId) {
        return fineRepository.findById(fineId).orElseThrow(()->new RuntimeException("Not found"));
    }

}
