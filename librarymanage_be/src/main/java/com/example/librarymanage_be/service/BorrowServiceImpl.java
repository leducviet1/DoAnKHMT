package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.BorrowItemRequest;
import com.example.librarymanage_be.dto.request.BorrowRequest;
import com.example.librarymanage_be.dto.response.BorrowDetailResponse;
import com.example.librarymanage_be.dto.response.BorrowResponse;
import com.example.librarymanage_be.enums.BorrowStatus;
import com.example.librarymanage_be.model.Book;
import com.example.librarymanage_be.model.Borrow;
import com.example.librarymanage_be.model.BorrowDetail;
import com.example.librarymanage_be.model.User;
import com.example.librarymanage_be.repo.BorrowDetailRepository;
import com.example.librarymanage_be.repo.BorrowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                throw new RuntimeException("Không đủ sách: " + book.getTitle());
            }
            book.setAvailableQuantity(book.getAvailableQuantity() - itemRequest.getQuantity());
            BorrowDetail detail = new BorrowDetail();
            detail.setBorrow(borrow);
            detail.setBook(book);
            detail.setQuantity(itemRequest.getQuantity());
            detail.setNote(itemRequest.getNote());
            details.add(detail);
        }
        borrowDetailRepository.saveAll(details);
        return toResponse(borrow,details);
    }


    @Override
    public BorrowResponse returnBooks(Integer borrowId) {
        Borrow borrowExisted = findById(borrowId);
        if(borrowExisted.getStatus().equals(BorrowStatus.RETURNED)) {
            throw new RuntimeException("Sách đã trả");
        }
        List<BorrowDetail> details = borrowDetailRepository.findByBorrow_BorrowId(borrowId);
        for(BorrowDetail detail : details) {
            Book book = detail.getBook();
            book.setAvailableQuantity(book.getAvailableQuantity() + detail.getQuantity());
        }
        borrowExisted.setReturnDate(LocalDateTime.now());
        borrowExisted.setStatus(BorrowStatus.RETURNED);
        borrowRepository.save(borrowExisted);
        return toResponse(borrowExisted,details);
    }
    @Override
    public Borrow findById(Integer borrowId) {
        return borrowRepository.findById(borrowId).orElseThrow(()-> new RuntimeException("Không tim thấy phiếu mượn:" + borrowId));
    }
}
