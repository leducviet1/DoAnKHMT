package com.example.librarymanage_be.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BorrowDetailResponse {
    private Integer bookId;
    private String title;
    private Integer quantity;
    private String note;
}
