package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.BookRequest;
import com.example.librarymanage_be.dto.response.BookResponse;
import com.example.librarymanage_be.dto.response.BookSuggestionResponse;
import com.example.librarymanage_be.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    // search, find-all, create, update, update 1 phan (patch), delete

    /**
     * Tìm kiếm sách theo title, tên thể loại, tên tác giả
     */
    Page<BookResponse> searchBooks(String title, String categoryName, String authorName, Pageable pageable);

    List<BookSuggestionResponse> suggestBooks(String keyword, int limit);

    /**
     *
     * Tạo sách mới
     */
    BookResponse create(BookRequest bookRequest);

    /**
     * Lấy danh sách sách có phân trang
     */
    Page<BookResponse> getBooks(Pageable pageable);

    /**
     * Lấy thông tin sách chi tiết theo id
     */
    BookResponse findById(Integer id);

    Book getEntityById(Integer id);


    /**
     * Cập nhật sách
     */
    BookResponse update(Integer bookId, BookRequest bookRequest);

    /**
     * Xóa sách theo id
     */
    void delete(Integer bookId);

}
