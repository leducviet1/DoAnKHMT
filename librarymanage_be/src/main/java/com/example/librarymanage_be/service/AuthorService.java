package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.AuthorRequest;
import com.example.librarymanage_be.dto.response.AuthorResponse;
import com.example.librarymanage_be.Entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuthorService {
    AuthorResponse create(AuthorRequest authorRequest);
    Page<AuthorResponse> getAuthors(Pageable pageable);
    AuthorResponse update(Integer authorId,AuthorRequest authorRequest);
    void delete(Integer authorId);
    Author findAuthorById(Integer authorId);
    AuthorResponse findById(Integer authorId);
    List<Author> findAllById(List<Integer> authorIds);
}
