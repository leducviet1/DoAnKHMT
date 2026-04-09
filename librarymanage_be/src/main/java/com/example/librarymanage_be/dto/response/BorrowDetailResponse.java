package com.example.librarymanage_be.dto.response;

import lombok.Data;

@Data
public class BorrowDetailResponse {
    private Integer bookId;
    private String title;
    private Integer quantity;
    private String note;
}
