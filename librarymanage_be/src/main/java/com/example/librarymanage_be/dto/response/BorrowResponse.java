package com.example.librarymanage_be.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BorrowResponse {
    private Integer borrowId;
    private Integer userId;
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private LocalDateTime dueDate;
    private String borrowStatus;
    private List<BorrowDetailResponse> details;
}
