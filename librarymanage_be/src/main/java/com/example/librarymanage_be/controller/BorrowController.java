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
    @PutMapping("/return/{id}")
    public ResponseEntity<BorrowResponse> returnBooks(@PathVariable Integer id) {
        return ResponseEntity.ok(borrowService.returnBooks(id));
    }
}
