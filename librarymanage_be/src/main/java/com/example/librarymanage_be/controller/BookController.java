package com.example.librarymanage_be.controller;

import com.example.librarymanage_be.dto.request.BookRequest;
import com.example.librarymanage_be.dto.response.BookResponse;
import com.example.librarymanage_be.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    @GetMapping
    public Page<BookResponse> getBooks(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "5") int size){
        Pageable pageable = PageRequest.of(page, size);
        return bookService.getBooks(pageable);
    }
    @PostMapping("/create")
    public BookResponse create(@Valid  @RequestBody  BookRequest bookRequest){
        return bookService.create(bookRequest);
    }
    @PutMapping("/update/{id}")
    public BookResponse update(@PathVariable Integer id, @Valid  @RequestBody BookRequest bookRequest){
        return bookService.update(id, bookRequest);
    }
    @DeleteMapping("delete/{id}")
    public void delete(@PathVariable Integer id){
        bookService.delete(id);
    }
}
