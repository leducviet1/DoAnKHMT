package com.example.librarymanage_be.service;

import com.example.librarymanage_be.config.FineConfig;
import com.example.librarymanage_be.dto.request.BorrowItemRequest;
import com.example.librarymanage_be.dto.request.BorrowRequest;
import com.example.librarymanage_be.dto.response.BorrowDetailResponse;
import com.example.librarymanage_be.dto.response.BorrowResponse;
import com.example.librarymanage_be.enums.BorrowDetailStatus;
import com.example.librarymanage_be.enums.BorrowStatus;
import com.example.librarymanage_be.enums.FineStatus;
import com.example.librarymanage_be.enums.FineType;
import com.example.librarymanage_be.Entity.*;
import com.example.librarymanage_be.repo.BorrowDetailRepository;
import com.example.librarymanage_be.repo.BorrowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {
    private final UserService userService;
    private final BorrowRepository borrowRepository;
    private final BookService bookService;
    private final BorrowDetailRepository borrowDetailRepository;

    @Override
    public BorrowResponse toResponse(Borrow borrow, List<BorrowDetail> details) {
        BorrowResponse borrowResponse = new BorrowResponse();
        borrowResponse.setBorrowId(borrow.getBorrowId());
        borrowResponse.setUserId(borrow.getUser().getUserId());
        borrowResponse.setBorrowDate(borrow.getBorrowDate());
        borrowResponse.setDueDate(borrow.getDueDate());
        borrowResponse.setReturnDate(borrow.getReturnDate());
        borrowResponse.setBorrowStatus(borrow.getStatus().toString());
        List<BorrowDetailResponse> detailResponses = details.stream()
                .map(detail -> {
                    BorrowDetailResponse d = new BorrowDetailResponse();
                    d.setBookId(detail.getBook().getBookId());
                    d.setTitle(detail.getBook().getTitle());
                    d.setQuantity(detail.getQuantity());
                    d.setNote(detail.getNote());
                    return d;
                })
                .toList();

        borrowResponse.setDetails(detailResponses);
        return borrowResponse;
    }

    @Override
    public BorrowResponse borrowBooks(BorrowRequest borrowRequest) {
        log.info("[BORROW] Borrowing book with userId={}", borrowRequest.getUserId());
        User user = userService.findById(borrowRequest.getUserId());
        Borrow borrow = new Borrow();
        borrow.setUser(user);
        borrow.setBorrowDate(LocalDateTime.now());
        borrow.setDueDate(LocalDateTime.now().plusDays(30));
        borrow.setStatus(BorrowStatus.BORROWING);
        borrowRepository.save(borrow);
        List<BorrowDetail> details = new ArrayList<>();
        for (BorrowItemRequest itemRequest : borrowRequest.getItems()) {
            Book book = bookService.findBookById(itemRequest.getBookId());
            if (book.getAvailableQuantity() < itemRequest.getQuantity()) {
                log.error("[BORROW] Book quantity less than available item quantity={} ", itemRequest.getQuantity());
                throw new RuntimeException("Không đủ sách: " + book.getTitle());
            }
            book.setAvailableQuantity(book.getAvailableQuantity() - itemRequest.getQuantity());
            BorrowDetail detail = new BorrowDetail();
            detail.setBorrow(borrow);
            detail.setBook(book);
            detail.setQuantity(itemRequest.getQuantity());
            detail.setNote(itemRequest.getNote());
            detail.setStatus(BorrowDetailStatus.BORROWING);
            details.add(detail);
        }
        borrowDetailRepository.saveAll(details);
        log.info("[BORROW] Borrowing successful with borrowId={}", borrow.getBorrowId());
        return toResponse(borrow, details);
    }

    @Override
    public void returnBook(Integer borrowDetailId) {
        BorrowDetail detail = borrowDetailRepository.findById(borrowDetailId).orElseThrow(() -> {
            log.error("[BORROW_DETAIL] BorrowDetail not found with id={}", borrowDetailId);
            return new RuntimeException("Not found");
        });

        if (detail.getStatus().equals(BorrowStatus.RETURNED)) {
            log.error("[BORROW_DETAIL] Borrow already returned");
            throw new RuntimeException("Sách đã trả");
        }
        Book book = detail.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + detail.getQuantity());
        detail.setStatus(BorrowDetailStatus.RETURNED);
        detail.setReturnDate(LocalDateTime.now());
        //Số ngày trễ
        long lateDays = 0;
        if (LocalDateTime.now().isAfter(detail.getReturnDate())) {
            lateDays = ChronoUnit.DAYS.between(detail.getReturnDate(), LocalDateTime.now());
        }
        if (lateDays > 0) {
            BigDecimal amount = BigDecimal.valueOf(lateDays).multiply(FineConfig.LATE_FEE_PER_DAY);
            Fine fine = new Fine();
            fine.setAmount(amount);
            fine.setBorrowDetail(detail);
            fine.setType(FineType.LATE);
            fine.setStatus(FineStatus.PENDING);
            fine.setReason("Trả muộn:" + lateDays + " ngày");
            fine.setCreatedAt(LocalDateTime.now());
        }
        borrowDetailRepository.save(detail);
        Borrow borrow = detail.getBorrow();
        boolean allReturned = borrow.getDetails().stream().allMatch(d -> d.getReturnDate() != null);
        if (allReturned) {
            borrow.setStatus(BorrowStatus.RETURNED);
            borrow.setReturnDate(LocalDateTime.now());
            borrowRepository.save(borrow);
            log.info("[BORROW] Borrow successful with borrowId={}", borrow.getBorrowId());
        }
    }

    @Override
    public BorrowResponse returnAllBooks(Integer borrowId) {
        Borrow borrow = findById(borrowId);
        if (borrow.getStatus().equals(BorrowStatus.RETURNED)) {
            log.error("[BORROW_DETAIL] BorrowDetail already returned");
            throw new RuntimeException("Phiếu này đã trả");
        }
        List<BorrowDetail> details = borrowDetailRepository.findByBorrow_BorrowId(borrowId);
        for (BorrowDetail detail : details) {
            Book book = detail.getBook();
            book.setAvailableQuantity(book.getAvailableQuantity() + detail.getQuantity());
            detail.setReturnDate(LocalDateTime.now());
            detail.setStatus(BorrowDetailStatus.RETURNED);
        }
        borrow.setReturnDate(LocalDateTime.now());
        borrow.setStatus(BorrowStatus.RETURNED);
        borrowRepository.save(borrow);
        log.info("[BORROW] Return successful with borrowId={}", borrow.getBorrowId());
        return toResponse(borrow, details);
    }

    @Override
    public Borrow findById(Integer borrowId) {
        log.info("[FIND] Borrowing book with id={}", borrowId);
        return borrowRepository.findById(borrowId).orElseThrow(() ->{
            log.error("[FIND] Book not found with id={}", borrowId);
            return new RuntimeException("Not found");
        });
    }
}
