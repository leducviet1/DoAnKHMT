package com.example.librarymanage_be.service;

import com.example.librarymanage_be.entity.BorrowDetail;
import com.example.librarymanage_be.repo.BorrowDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowDetailServiceImpl implements BorrowDetailService {
    private final BorrowDetailRepository borrowDetailRepository;
    @Override
    public BorrowDetail findById(Integer id) {
        log.info("[BORROW_DETAIL] Finding BorrowDetail with id={}]");
        return borrowDetailRepository.findById(id).orElseThrow(()->{
            log.error("BORROW_DETAIL not found with id={}", id);
            return new RuntimeException("Not found");
        });
    }
}
