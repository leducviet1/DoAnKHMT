package com.example.librarymanage_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopBorrowingUserResponse {
    private Integer userId;
    private String username;
    private String email;
    private Long borrowedQuantity;
}
