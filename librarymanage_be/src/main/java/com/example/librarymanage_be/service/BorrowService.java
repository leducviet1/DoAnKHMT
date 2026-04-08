package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.BorrowRequest;
import com.example.librarymanage_be.dto.response.BorrowResponse;
import com.example.librarymanage_be.Entity.Borrow;
import com.example.librarymanage_be.Entity.BorrowDetail;

import java.util.List;

public interface  BorrowService {
    BorrowResponse toResponse(Borrow borrow, List<BorrowDetail> details);
    BorrowResponse borrowBooks(BorrowRequest borrowRequest);
    void returnBook(Integer borrowDetailId);
    BorrowResponse returnAllBooks(Integer borrowId);
    Borrow findById(Integer borrowId);


}
