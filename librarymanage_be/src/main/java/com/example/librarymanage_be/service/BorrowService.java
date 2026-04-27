package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.BorrowRequest;
import com.example.librarymanage_be.dto.response.BorrowResponse;
import com.example.librarymanage_be.entity.Borrow;
import com.example.librarymanage_be.entity.BorrowDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface BorrowService {
    BorrowResponse toResponse(Borrow borrow, List<BorrowDetail> details);

    Page<BorrowResponse> getBorrows( Pageable pageable);

    Page<BorrowResponse> getBorrowsByUserEmail(String email, Pageable pageable);

    BorrowResponse borrowBooks(BorrowRequest borrowRequest);

    void returnBook(Integer borrowDetailId);

    BorrowResponse returnAllBooks(Integer borrowId);

    Borrow findById(Integer borrowId);

    byte[] getBorrowContract(Integer borrowId, String requesterEmail, boolean canViewAll) throws IOException;


}
