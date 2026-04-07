package com.example.librarymanage_be.service;

import com.example.librarymanage_be.model.BorrowDetail;
import com.example.librarymanage_be.repo.BorrowDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BorrowDetailServiceImpl implements BorrowDetailService {
    private final BorrowDetailRepository borrowDetailRepository;
    @Override
    public BorrowDetail findById(Integer id) {
        return borrowDetailRepository.findById(id).orElseThrow(()->new RuntimeException("Not found"));
    }
}
