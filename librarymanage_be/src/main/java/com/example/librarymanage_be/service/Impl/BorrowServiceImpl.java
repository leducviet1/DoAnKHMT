package com.example.librarymanage_be.service.Impl;

import com.example.librarymanage_be.config.FineConfig;
import com.example.librarymanage_be.dto.request.BorrowItemRequest;
import com.example.librarymanage_be.dto.request.BorrowRequest;
import com.example.librarymanage_be.dto.response.BorrowDetailResponse;
import com.example.librarymanage_be.dto.response.BorrowResponse;
import com.example.librarymanage_be.entity.Book;
import com.example.librarymanage_be.entity.Borrow;
import com.example.librarymanage_be.entity.BorrowDetail;
import com.example.librarymanage_be.entity.Fine;
import com.example.librarymanage_be.entity.User;
import com.example.librarymanage_be.enums.BorrowDetailStatus;
import com.example.librarymanage_be.enums.BorrowStatus;
import com.example.librarymanage_be.enums.FineStatus;
import com.example.librarymanage_be.enums.FineType;
import com.example.librarymanage_be.repo.BorrowDetailRepository;
import com.example.librarymanage_be.repo.BorrowRepository;
import com.example.librarymanage_be.repo.FineRepository;
import com.example.librarymanage_be.service.BookService;
import com.example.librarymanage_be.service.BorrowContractService;
import com.example.librarymanage_be.service.BorrowService;
import com.example.librarymanage_be.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BorrowServiceImpl implements BorrowService {
    private final UserService userService;
    private final BorrowRepository borrowRepository;
    private final BookService bookService;
    private final BorrowDetailRepository borrowDetailRepository;
    private final FineRepository fineRepository;
    private final BorrowContractService borrowContractService;

    @Override
    public BorrowResponse toResponse(Borrow borrow, List<BorrowDetail> details) {
        BorrowResponse borrowResponse = new BorrowResponse();
        borrowResponse.setBorrowId(borrow.getBorrowId());
        borrowResponse.setUserId(borrow.getUser().getUserId());
        borrowResponse.setUserName(borrow.getUser().getUsername());
        borrowResponse.setUserEmail(borrow.getUser().getEmail());
        borrowResponse.setBorrowDate(borrow.getBorrowDate());
        borrowResponse.setDueDate(borrow.getDueDate());
        borrowResponse.setReturnDate(borrow.getReturnDate());
        borrowResponse.setBorrowStatus(borrow.getStatus().toString());
        borrowResponse.setContractAvailable(borrowContractService.exists(borrow.getBorrowId()));
        borrowResponse.setContractDownloadUrl("/api/borrow/" + borrow.getBorrowId() + "/contract");
        List<BorrowDetailResponse> detailResponses = details.stream()
                .map(detail -> {
                    BorrowDetailResponse detailResponse = new BorrowDetailResponse();
                    detailResponse.setId(detail.getId());
                    detailResponse.setTitle(detail.getBook().getTitle());
                    detailResponse.setQuantity(detail.getQuantity());
                    detailResponse.setNote(detail.getNote());
                    detailResponse.setStatus(detail.getStatus().toString());
                    return detailResponse;
                })
                .toList();

        borrowResponse.setDetails(detailResponses);
        return borrowResponse;
    }

    @Override
    public Page<BorrowResponse> getBorrows(Pageable pageable) {
        Page<Borrow> borrows = borrowRepository.findAll(pageable);
        return borrows.map(borrow -> toResponse(
                borrow,
                borrowDetailRepository.findByBorrow_BorrowId(borrow.getBorrowId())
        ));
    }

    @Override
    public Page<BorrowResponse> getBorrowsByUserEmail(String email, Pageable pageable) {
        Page<Borrow> borrows = borrowRepository.findByUser_Email(email, pageable);
        return borrows.map(borrow -> toResponse(
                borrow,
                borrowDetailRepository.findByBorrow_BorrowId(borrow.getBorrowId())
        ));
    }

    @Override
    public BorrowResponse borrowBooks(BorrowRequest borrowRequest) {
        log.info("[BORROW] Borrowing book with userId={}", borrowRequest.getUserId());
        User user = userService.findById(borrowRequest.getUserId());

        Borrow borrow = new Borrow();
        borrow.setUser(user);
        borrow.setBorrowDate(LocalDateTime.now());
        borrow.setDueDate(resolveDueDate(borrowRequest.getDueDate()));
        borrow.setStatus(BorrowStatus.BORROWING);
        borrowRepository.save(borrow);

        List<BorrowDetail> details = new ArrayList<>();
        for (BorrowItemRequest itemRequest : borrowRequest.getItems()) {
            Book book = bookService.getEntityById(itemRequest.getBookId());
            if (book.getAvailableQuantity() < itemRequest.getQuantity()) {
                log.error("[BORROW] Book quantity less than available item quantity={}", itemRequest.getQuantity());
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
        try {
            borrowContractService.generateContract(borrow, details);
        } catch (IOException e) {
            throw new RuntimeException("Cannot generate borrow contract", e);
        }

        log.info("[BORROW] Borrowing successful with borrowId={}", borrow.getBorrowId());
        return toResponse(borrow, details);
    }

    @Override
    public void returnBook(Integer borrowDetailId) {
        BorrowDetail detail = borrowDetailRepository.findById(borrowDetailId)
                .orElseThrow(() -> new RuntimeException("Not found borrow detail"));

        if (detail.getStatus() == BorrowDetailStatus.RETURNED) {
            throw new RuntimeException("Sach da tra");
        }

        Borrow borrow = detail.getBorrow();
        Book book = detail.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + detail.getQuantity());
        detail.setStatus(BorrowDetailStatus.RETURNED);
        detail.setReturnDate(LocalDateTime.now());
        createLateFineIfNeeded(detail, borrow.getDueDate());
        borrowDetailRepository.save(detail);

        boolean allReturned = borrowDetailRepository.findByBorrow_BorrowId(borrow.getBorrowId())
                .stream()
                .allMatch(item -> item.getReturnDate() != null);
        if (allReturned) {
            borrow.setStatus(BorrowStatus.RETURNED);
            borrow.setReturnDate(LocalDateTime.now());
            borrowRepository.save(borrow);
        }
    }

    @Override
    public BorrowResponse returnAllBooks(Integer borrowId) {
        Borrow borrow = findById(borrowId);
        if (borrow.getStatus() == BorrowStatus.RETURNED) {
            throw new RuntimeException("Phieu nay da tra");
        }

        List<BorrowDetail> details = borrowDetailRepository.findByBorrow_BorrowId(borrowId);
        for (BorrowDetail detail : details) {
            Book book = detail.getBook();
            book.setAvailableQuantity(book.getAvailableQuantity() + detail.getQuantity());
            detail.setReturnDate(LocalDateTime.now());
            detail.setStatus(BorrowDetailStatus.RETURNED);
            createLateFineIfNeeded(detail, borrow.getDueDate());
        }

        borrow.setReturnDate(LocalDateTime.now());
        borrow.setStatus(BorrowStatus.RETURNED);
        borrowRepository.save(borrow);
        return toResponse(borrow, details);
    }

    @Override
    public Borrow findById(Integer borrowId) {
        return borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Not found borrow"));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getBorrowContract(Integer borrowId, String requesterEmail, boolean canViewAll) throws IOException {
        Borrow borrow = findById(borrowId);
        if (!canViewAll && !borrow.getUser().getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new RuntimeException("You do not have permission to view this contract");
        }
        if (!borrowContractService.exists(borrowId)) {
            throw new RuntimeException("Borrow contract not found");
        }
        return borrowContractService.getContractContent(borrowId);
    }

    private LocalDateTime resolveDueDate(LocalDate dueDate) {
        LocalDate effectiveDueDate = dueDate != null ? dueDate : LocalDate.now().plusDays(30);
        return effectiveDueDate.atTime(LocalTime.MAX);
    }

    private void createLateFineIfNeeded(BorrowDetail detail, LocalDateTime dueDate) {
        if (detail.getReturnDate() == null || dueDate == null || !detail.getReturnDate().isAfter(dueDate)) {
            return;
        }

        long lateDays = ChronoUnit.DAYS.between(dueDate.toLocalDate(), detail.getReturnDate().toLocalDate());
        if (lateDays <= 0) {
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(lateDays).multiply(FineConfig.LATE_FEE_PER_DAY);
        Fine fine = new Fine();
        fine.setAmount(amount);
        fine.setBorrowDetail(detail);
        fine.setType(FineType.LATE);
        fine.setStatus(FineStatus.PENDING);
        fine.setReason("Tre han " + lateDays + " ngay");
        fine.setCreatedAt(LocalDateTime.now());
        fineRepository.save(fine);
    }
}
