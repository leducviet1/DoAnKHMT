package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.model.BookAuthor;
import com.example.librarymanage_be.model.BookAuthorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookAuthorRepository extends JpaRepository<BookAuthor, BookAuthorId> {
    void deleteByBook_BookId(Integer bookBookId);
}
