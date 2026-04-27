package com.example.librarymanage_be.controller;

import com.example.librarymanage_be.dto.request.BookRequest;
import com.example.librarymanage_be.dto.response.BookResponse;
import com.example.librarymanage_be.dto.response.BookSuggestionResponse;
import com.example.librarymanage_be.dto.response.ExcelFileResponse;
import com.example.librarymanage_be.export.ExcelExportService;
import com.example.librarymanage_be.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookController {
    private final BookService bookService;
    private final ExcelExportService excelExportService;

    @GetMapping
    public Page<BookResponse> getBooks(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookService.getBooks(pageable);
    }

    @GetMapping("/search")
    public Page<BookResponse> searchBooks(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "5") int size,
                                          @RequestParam(required = false) String title,
                                          @RequestParam(required = false) String categoryName,
                                          @RequestParam(required = false) String authorName) {
        Pageable pageable = PageRequest.of(page, size);
        return bookService.searchBooks(title, categoryName, authorName, pageable);
    }

    @GetMapping("/suggest")
    public List<BookSuggestionResponse> suggestBooks(@RequestParam String keyword,
                                                     @RequestParam(defaultValue = "8") int limit) {
        return bookService.suggestBooks(keyword, limit);
    }

    @PostMapping("/create")
    public BookResponse create(@Valid @RequestBody BookRequest bookRequest) {
        return bookService.create(bookRequest);
    }

    @PutMapping("/update/{id}")
    public BookResponse update(@PathVariable Integer id, @Valid @RequestBody BookRequest bookRequest) {
        return bookService.update(id, bookRequest);
    }

    @DeleteMapping("delete/{id}")
    public void delete(@PathVariable Integer id) {
        bookService.delete(id);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "5") int size,
                                         @RequestParam(required = false) String title,
                                         @RequestParam(required = false) String categoryName,
                                         @RequestParam(required = false) String authorName) throws IOException {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> books = hasSearchFilter(title, categoryName, authorName)
                ? bookService.searchBooks(title, categoryName, authorName, pageable)
                : bookService.getBooks(pageable);
        ExcelFileResponse file = excelExportService.exportBooks(books);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(file.getContent().length)
                .body(file.getContent());
    }

    private boolean hasSearchFilter(String title, String categoryName, String authorName) {
        return isNotBlank(title) || isNotBlank(categoryName) || isNotBlank(authorName);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
