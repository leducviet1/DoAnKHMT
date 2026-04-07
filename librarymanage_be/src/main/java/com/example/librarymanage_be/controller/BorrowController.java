package com.example.librarymanage_be.controller;

import com.example.librarymanage_be.dto.request.BorrowRequest;
import com.example.librarymanage_be.dto.response.BorrowResponse;
import com.example.librarymanage_be.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {
    private final BorrowService borrowService;

    @PostMapping
    public ResponseEntity<BorrowResponse> borrowBooks(
            @RequestBody BorrowRequest request) {
        return ResponseEntity.ok(borrowService.borrowBooks(request));
    }
    @PostMapping("/return-all/{borrowId}")
    public ResponseEntity<BorrowResponse> returnBooks(@PathVariable Integer borrowId) {
        return ResponseEntity.ok(borrowService.returnAllBooks(borrowId));
    }
    @PostMapping("/return-item/{borrowDetailId}")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Integer borrowDetailId) {
        borrowService.returnBook(borrowDetailId);
        return ResponseEntity.ok().build();
    }
}
