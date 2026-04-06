package com.example.librarymanage_be.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BorrowItemRequest {
    private Integer bookId;
    private Integer quantity;
    private String note;
}
