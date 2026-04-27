package com.example.librarymanage_be.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Data
public class BorrowRequest {
    private Integer userId;
    private LocalDate dueDate;
    private List<BorrowItemRequest> items;

}
