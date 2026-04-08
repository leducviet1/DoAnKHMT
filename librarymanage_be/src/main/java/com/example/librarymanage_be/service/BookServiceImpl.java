package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.BookRequest;
import com.example.librarymanage_be.dto.response.BookResponse;
import com.example.librarymanage_be.enums.BookStatus;
import com.example.librarymanage_be.mapper.BookMapper;
import com.example.librarymanage_be.Entity.Author;
import com.example.librarymanage_be.Entity.Book;
import com.example.librarymanage_be.Entity.BookAuthor;
import com.example.librarymanage_be.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
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
        log.info("[BOOK] Creating a new book with title={}",bookRequest.getTitle());
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
        log.info("[BOOK] Created book with name={}",savedBook.getTitle());
        return bookMapper.toResponse(savedBook);
    }

    @Override
    public Page<BookResponse> getBooks(Pageable pageable) {
        log.info("[BOOK] Getting books with page={},size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Book> books = bookRepository.findAll(pageable);
        log.info("[BOOK] Found {} publishers", books.getTotalElements());
        return books.map(bookMapper::toResponse);
    }

    @Override
    public BookResponse findById(Integer id) {
        Book book = findBookById(id);
        log.info("[BOOK] Found successfully a new Author with id={}", book.getBookId());
        return bookMapper.toResponse(book);
    }

    @Override
    public Book findBookById(Integer id) {
        log.info("[BOOK] Finding Book with id={}", id);
        return bookRepository.findById(id).orElseThrow(() -> {
            log.error("[AUTHOR] Author not found");
            return new RuntimeException("Book not found with id: " + id);
        });
    }

    @Override
    public BookResponse update(Integer bookId, BookRequest bookRequest) {
        log.info("[BOOK] Updating Author with id={}", bookId);
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
        log.info("[BOOK] Updated successfully a new book with id={},name={}", updatedBook.getBookId(), updatedBook.getTitle());
        return bookMapper.toResponse(updatedBook);
    }

    @Override
    public void delete(Integer bookId) {
        Book bookExisted = findBookById(bookId);
        bookRepository.delete(bookExisted);
        log.info("[BOOK] Deleted book with id={}", bookId);
    }
}
