package com.example.librarymanage_be.dto.response;

import com.example.librarymanage_be.enums.BookStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Integer bookId;
    private String title;
    private BigDecimal price;
    private Integer availableQuantity;
    private String description;
    private String categoryName;
    private String publisherName;
    private List<String> authorNames;
    private BookStatus bookStatus;
}
