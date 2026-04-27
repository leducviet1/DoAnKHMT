package com.example.librarymanage_be.controller;

import com.example.librarymanage_be.dto.request.BorrowRequest;
import com.example.librarymanage_be.dto.response.BorrowResponse;
import com.example.librarymanage_be.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

public class BorrowController {
    private final BorrowService borrowService;
    @GetMapping
    public Page<BorrowResponse> getBorrows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return borrowService.getBorrows(pageable);
    }

    @GetMapping("/me")
    public Page<BorrowResponse> getMyBorrows(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "6") int size,
                                             Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);
        return borrowService.getBorrowsByUserEmail(authentication.getName(), pageable);
    }

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

    @GetMapping("/{borrowId}/contract")
    public ResponseEntity<byte[]> downloadContract(@PathVariable Integer borrowId,
                                                   Authentication authentication) throws IOException {
        boolean canViewAll = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .anyMatch(authority -> authority.equals("ADMIN") || authority.equals("LIBRARIAN"));
        byte[] content = borrowService.getBorrowContract(borrowId, authentication.getName(), canViewAll);
        String fileName = "borrow_contract_" + borrowId + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(content.length)
                .body(content);
    }
}
