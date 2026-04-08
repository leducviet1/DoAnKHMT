package com.example.librarymanage_be.Entity;

import com.example.librarymanage_be.enums.BookStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookId;
    private String title;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private String description;

    @OneToMany(cascade = CascadeType.ALL,mappedBy = "book")
    private List<BookAuthor> bookAuthors;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @Enumerated(EnumType.STRING)
    private BookStatus bookStatus;

}
