package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.BookRequest;
import com.example.librarymanage_be.dto.response.BookResponse;
import com.example.librarymanage_be.Entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookResponse create(BookRequest bookRequest);
    Page<BookResponse> getBooks(Pageable pageable);
    BookResponse findById(Integer id);
    Book findBookById(Integer id);
    BookResponse update(Integer bookId,BookRequest bookRequest);
    void  delete(Integer bookId);
}
