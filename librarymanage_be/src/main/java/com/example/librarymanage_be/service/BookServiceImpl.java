package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.BookRequest;
import com.example.librarymanage_be.dto.response.BookResponse;
import com.example.librarymanage_be.enums.BookStatus;
import com.example.librarymanage_be.mapper.BookMapper;
import com.example.librarymanage_be.model.Author;
import com.example.librarymanage_be.model.Book;
import com.example.librarymanage_be.model.BookAuthor;
import com.example.librarymanage_be.repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final AuthorService authorService;
    private final CategoryService categoryService;
    private final PublisherService publisherService;
    private final BookAuthorRepository bookAuthorRepository;

    @Override

    public BookResponse create(BookRequest bookRequest) {
        Book bookMap = bookMapper.toEntity(bookRequest);
        bookMap.setAvailableQuantity(bookRequest.getTotalQuantity());
        bookMap.setPrice(bookRequest.getPrice());
        if (bookMap.getAvailableQuantity() > 0) {
            bookMap.setBookStatus(BookStatus.AVAILABLE);
        } else {
            bookMap.setBookStatus(BookStatus.INACTIVE);
        }
        bookMap.setCategory(categoryService.findById(bookRequest.getCategoryId()));
        bookMap.setPublisher(publisherService.findById(bookRequest.getPublisherId()));

        List<Integer> authorIds = bookRequest.getAuthorIds()
                .stream()
                .distinct()
                .toList();

        List<Author> authors = authorService.findAllById(authorIds);

        List<BookAuthor> bookAuthors = authors.stream()
                .map(author -> {
                    BookAuthor ba = new BookAuthor();
                    ba.setBook(bookMap);
                    ba.setAuthor(author);
                    return ba;
                })
                .toList();
        bookMap.setBookAuthors(bookAuthors);
        Book savedBook = bookRepository.save(bookMap);
        return bookMapper.toResponse(savedBook);
    }

    @Override
    public Page<BookResponse> getBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findAll(pageable);
        return books.map(bookMapper::toResponse);
    }

    @Override
    public BookResponse findById(Integer id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        return bookMapper.toResponse(book);
    }

    @Override
    public Book findBookById(Integer id) {
        return bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }

    @Override
    public BookResponse update(Integer bookId, BookRequest bookRequest) {
        Book bookExist = findBookById(bookId);
        bookMapper.updateBook(bookExist, bookRequest);
        if (bookRequest.getCategoryId() != null) {
            bookExist.setCategory(categoryService.findById(bookRequest.getCategoryId()));
        }
        if (bookRequest.getPublisherId() != null) {
            bookExist.setPublisher(publisherService.findById(bookRequest.getPublisherId()));
        }

        if (bookRequest.getAuthorIds() != null) {

            List<Integer> authorIds = bookRequest.getAuthorIds()
                    .stream()
                    .distinct()
                    .toList();
            bookAuthorRepository.deleteByBook_BookId(bookId);
            bookAuthorRepository.flush();
            //Lấy author mới
            List<Author> authors = authorService.findAllById(authorIds);
            List<BookAuthor> bookAuthors = authors.stream().map(author -> {
                BookAuthor ba = new BookAuthor();
                ba.setBook(bookExist);
                ba.setAuthor(author);
                return ba;
            }).toList();
            bookAuthorRepository.saveAll(bookAuthors);
        }
        bookExist.setPrice(bookRequest.getPrice());
        if (bookRequest.getTotalQuantity() != null) {
            Integer borrowed = bookExist.getTotalQuantity() - bookExist.getAvailableQuantity();
            bookExist.setTotalQuantity(bookRequest.getTotalQuantity());
            bookExist.setAvailableQuantity(bookRequest.getTotalQuantity() - borrowed);
            if (bookExist.getAvailableQuantity() > 0) {
                bookExist.setBookStatus(BookStatus.AVAILABLE);
            } else {
                bookExist.setBookStatus(BookStatus.OUT_OF_STOCK);
            }
        }
        Book updatedBook = bookRepository.save(bookExist);
        return bookMapper.toResponse(updatedBook);
    }

    @Override
    public void delete(Integer bookId) {
        Book bookExisted = findBookById(bookId);
        bookRepository.delete(bookExisted);
    }
}
