package com.example.librarymanage_be.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class BorrowResponse {
    private Integer borrowId;
    private Integer userId;
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private LocalDateTime dueDate;
    private String borrowStatus;
    private List<BorrowDetailResponse> details;
}
