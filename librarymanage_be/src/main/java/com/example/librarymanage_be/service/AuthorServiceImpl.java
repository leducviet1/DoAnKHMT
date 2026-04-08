package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.AuthorRequest;
import com.example.librarymanage_be.dto.response.AuthorResponse;
import com.example.librarymanage_be.mapper.AuthorMapper;
import com.example.librarymanage_be.model.Author;
import com.example.librarymanage_be.repo.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Override
    public AuthorResponse create(AuthorRequest authorRequest) {
        log.info("[AUTHOR] Creating a new Author with name={}", authorRequest.getAuthorName());
        Author authorMapped = authorMapper.toEntity(authorRequest);
        Author authorSaved = authorRepository.save(authorMapped);
        log.info("[AUTHOR] Created successfully a new Author with name={}", authorRequest.getAuthorName());
        return authorMapper.toResponse(authorSaved);
    }

    @Override
    public Page<AuthorResponse> getAuthors(Pageable pageable) {
        Page<Author> authors = authorRepository.findAll(pageable);
        return authors.map(authorMapper::toResponse);
    }

    @Override
    public AuthorResponse update(Integer authorId, AuthorRequest authorRequest) {
        Author authorExist = findAuthorById(authorId);
        authorMapper.updateEntity(authorRequest, authorExist);
        Author authorUpdated = authorRepository.save(authorExist);
        return authorMapper.toResponse(authorUpdated);
    }

    @Override
    public void delete(Integer authorId) {
        Author authorExist = findAuthorById(authorId);
        authorRepository.delete(authorExist);
    }

    @Override
    public Author findAuthorById(Integer authorId) {
        return authorRepository.findById(authorId).
                orElseThrow(() -> new RuntimeException("Author Not Found"));

    }

    @Override
    public AuthorResponse findById(Integer authorId) {
        Author author = findAuthorById(authorId);
        return authorMapper.toResponse(author);
    }

    @Override
    public List<Author> findAllById(List<Integer> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return List.of();
        }
        List<Author> authors = authorRepository.findAllById(authorIds);
        return authors;
    }
}
