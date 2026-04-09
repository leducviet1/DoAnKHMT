package com.example.librarymanage_be.dto.request;

import lombok.Data;

import java.util.List;
@Data
public class BorrowRequest {
    private Integer userId;
    private List<BorrowItemRequest> items;

}
