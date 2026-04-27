package com.example.librarymanage_be.dto.response;

import com.example.librarymanage_be.enums.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BookSuggestionResponse {
    private Integer bookId;
    private String title;
    private String categoryName;
    private List<String> authorNames;
    private Integer availableQuantity;
    private BookStatus bookStatus;
}
