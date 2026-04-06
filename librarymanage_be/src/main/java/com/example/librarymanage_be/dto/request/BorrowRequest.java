package com.example.librarymanage_be.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class BorrowRequest {
    private Integer userId;
    private List<BorrowItemRequest> items;

}
