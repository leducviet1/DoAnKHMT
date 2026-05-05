package com.example.librarymanage_be.service.Impl;

import com.example.librarymanage_be.dto.request.AuthorRequest;
import com.example.librarymanage_be.dto.response.AuthorResponse;
import com.example.librarymanage_be.entity.Author;
import com.example.librarymanage_be.mapper.AuthorMapper;
import com.example.librarymanage_be.repo.AuthorRepository;
import com.example.librarymanage_be.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        authorRepository.delete(findAuthorById(authorId));
    }

    @Override
    public Author findAuthorById(Integer authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author Not Found"));
    }

    @Override
    public AuthorResponse findById(Integer authorId) {
        return authorMapper.toResponse(findAuthorById(authorId));
    }

    @Override
    public List<Author> findListAuthorsById(List<Integer> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return List.of();
        }
        return authorRepository.findAllById(authorIds);
    }

    @Override
    public List<Author> resolveAuthors(List<Integer> authorIds, List<String> newAuthorNames) {
        Map<String, Author> authorMap = new LinkedHashMap<>();

        for (Author author : findListAuthorsById(authorIds)) {
            authorMap.put(normalizeAuthorName(author.getAuthorName()), author);
        }

        if (newAuthorNames != null) {
            for (String authorName : newAuthorNames) {
                if (authorName == null || authorName.isBlank()) {
                    continue;
                }

                String trimmedName = authorName.trim();
                String normalizedName = normalizeAuthorName(trimmedName);
                if (authorMap.containsKey(normalizedName)) {
                    continue;
                }

                Author author = authorRepository.findByAuthorNameIgnoreCase(trimmedName)
                        .orElseGet(() -> authorRepository.save(Author.builder()
                                .authorName(trimmedName)
                                .build()));
                authorMap.put(normalizeAuthorName(author.getAuthorName()), author);
            }
        }

        return new ArrayList<>(authorMap.values());
    }

    private String normalizeAuthorName(String authorName) {
        return authorName == null ? "" : authorName.trim().toLowerCase(Locale.ROOT);
    }
}
