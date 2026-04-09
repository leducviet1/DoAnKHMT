package com.example.librarymanage_be.dto.request;

import lombok.Data;
@Data
public class BorrowItemRequest {
    private Integer bookId;
    private Integer quantity;
    private String note;
}
