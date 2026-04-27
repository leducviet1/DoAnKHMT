package com.example.librarymanage_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopBorrowedBookResponse {
    private Integer bookId;
    private String title;
    private Long borrowedQuantity;
}
