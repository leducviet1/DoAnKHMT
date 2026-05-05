package com.example.librarymanage_be.service.Impl;

import com.example.librarymanage_be.dto.request.BookRequest;
import com.example.librarymanage_be.dto.response.BookResponse;
import com.example.librarymanage_be.dto.response.BookSuggestionResponse;
import com.example.librarymanage_be.entity.Author;
import com.example.librarymanage_be.entity.Book;
import com.example.librarymanage_be.entity.BookAuthor;
import com.example.librarymanage_be.enums.BookStatus;
import com.example.librarymanage_be.mapper.BookMapper;
import com.example.librarymanage_be.repo.BookAuthorRepository;
import com.example.librarymanage_be.repo.BookRepository;
import com.example.librarymanage_be.service.AuthorService;
import com.example.librarymanage_be.service.BookService;
import com.example.librarymanage_be.service.CategoryService;
import com.example.librarymanage_be.service.PublisherService;
import com.example.librarymanage_be.specification.BookSpecification;
import com.example.librarymanage_be.utils.EntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

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
        log.info("[BOOK] Creating a new book with title={}", bookRequest.getTitle());
        Book book = bookMapper.toEntity(bookRequest);
        book.setAvailableQuantity(bookRequest.getTotalQuantity());
        book.setPrice(bookRequest.getPrice());
        book.setBookStatus(book.getAvailableQuantity() > 0 ? BookStatus.AVAILABLE : BookStatus.INACTIVE);
        book.setCategory(categoryService.findById(bookRequest.getCategoryId()));
        book.setPublisher(publisherService.findById(bookRequest.getPublisherId()));

        List<Author> authors = authorService.resolveAuthors(bookRequest.getAuthorIds(), bookRequest.getNewAuthorNames());
        if (authors.isEmpty()) {
            throw new RuntimeException("Book must have at least one author");
        }

        book.setBookAuthors(buildBookAuthors(book, authors));
        Book savedBook = bookRepository.save(book);
        return bookMapper.toResponse(savedBook);
    }

    @Override
    public Page<BookResponse> getBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findAll(pageable);
        return books.map(bookMapper::toResponse);
    }

    @Override
    public BookResponse findById(Integer id) {
        return bookMapper.toResponse(getEntityById(id));
    }

    @Override
    public Book getEntityById(Integer id) {
        return EntityUtils.getOrThrow(bookRepository.findById(id), "Book not found with id=" + id);
    }

    @Override
    public BookResponse update(Integer bookId, BookRequest bookRequest) {
        Book bookExist = EntityUtils.getOrThrow(bookRepository.findById(bookId), "Book not found");
        bookMapper.updateBook(bookExist, bookRequest);

        if (bookRequest.getCategoryId() != null) {
            bookExist.setCategory(categoryService.findById(bookRequest.getCategoryId()));
        }
        if (bookRequest.getPublisherId() != null) {
            bookExist.setPublisher(publisherService.findById(bookRequest.getPublisherId()));
        }

        if (hasAuthorPayload(bookRequest)) {
            List<Author> authors = authorService.resolveAuthors(bookRequest.getAuthorIds(), bookRequest.getNewAuthorNames());
            bookAuthorRepository.deleteByBook_BookId(bookId);
            bookAuthorRepository.flush();
            bookAuthorRepository.saveAll(buildBookAuthors(bookExist, authors));
        }

        bookExist.setPrice(bookRequest.getPrice());
        if (bookRequest.getTotalQuantity() != null) {
            Integer borrowed = bookExist.getTotalQuantity() - bookExist.getAvailableQuantity();
            bookExist.setTotalQuantity(bookRequest.getTotalQuantity());
            bookExist.setAvailableQuantity(bookRequest.getTotalQuantity() - borrowed);
            bookExist.setBookStatus(bookExist.getAvailableQuantity() > 0 ? BookStatus.AVAILABLE : BookStatus.OUT_OF_STOCK);
        }

        Book updatedBook = bookRepository.save(bookExist);
        return bookMapper.toResponse(updatedBook);
    }

    @Override
    public void delete(Integer bookId) {
        bookRepository.delete(EntityUtils.getOrThrow(bookRepository.findById(bookId), "Book not found"));
    }

    @Override
    public Page<BookResponse> searchBooks(String title, String categoryName, String authorName, Pageable pageable) {
        Specification<Book> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (title != null) {
            spec = spec.and(BookSpecification.hasTitle(title));
        }
        if (categoryName != null) {
            spec = spec.and(BookSpecification.hasCategory(categoryName));
        }
        if (authorName != null) {
            spec = spec.and(BookSpecification.hasAuthor(authorName));
        }
        return bookRepository.findAll(spec, pageable).map(bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookSuggestionResponse> suggestBooks(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        int safeLimit = Math.max(1, Math.min(limit, 20));
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return bookRepository.findSuggestions(normalizedKeyword, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toSuggestionResponse)
                .toList();
    }

    private BookSuggestionResponse toSuggestionResponse(Book book) {
        List<String> authorNames = book.getBookAuthors() == null ? List.of() : book.getBookAuthors().stream()
                .map(bookAuthor -> bookAuthor.getAuthor().getAuthorName())
                .toList();

        return new BookSuggestionResponse(
                book.getBookId(),
                book.getTitle(),
                book.getCategory() == null ? null : book.getCategory().getCategoryName(),
                authorNames,
                book.getAvailableQuantity(),
                book.getBookStatus()
        );
    }

    private List<BookAuthor> buildBookAuthors(Book book, List<Author> authors) {
        return authors.stream()
                .map(author -> {
                    BookAuthor bookAuthor = new BookAuthor();
                    bookAuthor.setBook(book);
                    bookAuthor.setAuthor(author);
                    return bookAuthor;
                })
                .toList();
    }

    private boolean hasAuthorPayload(BookRequest bookRequest) {
        return bookRequest.getAuthorIds() != null || bookRequest.getNewAuthorNames() != null;
    }
}
