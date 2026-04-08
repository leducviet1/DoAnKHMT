package com.example.librarymanage_be.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@IdClass(BookAuthorId.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookAuthor {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;


}
