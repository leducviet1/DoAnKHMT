package com.example.librarymanage_be.controller;

import com.example.librarymanage_be.dto.request.AuthorRequest;
import com.example.librarymanage_be.dto.response.AuthorResponse;
import com.example.librarymanage_be.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<Page<AuthorResponse>>  getAuthors(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(authorService.getAuthors(pageable)) ;
    }

    @GetMapping("/{id}")
    public AuthorResponse getAuthor(@PathVariable Integer id) {
        return authorService.findById(id);
    }

    @PostMapping("/create")
    public AuthorResponse createAuthor(@RequestBody AuthorRequest authorRequest) {
        return authorService.create(authorRequest);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteAuthor(@PathVariable Integer id) {
        authorService.delete(id);
    }

    @PutMapping("update/{id}")
    public AuthorResponse updateAuthor(@PathVariable Integer id, @RequestBody AuthorRequest authorRequest) {
        return authorService.update(id, authorRequest);
    }
}
