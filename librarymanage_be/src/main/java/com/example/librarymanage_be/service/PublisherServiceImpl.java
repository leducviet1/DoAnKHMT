package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.PublisherRequest;
import com.example.librarymanage_be.dto.response.PublisherResponse;
import com.example.librarymanage_be.mapper.PublisherMapper;
import com.example.librarymanage_be.Entity.Publisher;
import com.example.librarymanage_be.repo.PublisherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Override
    public PublisherResponse createPublisher(PublisherRequest publisherRequest) {
        log.info("[PUBLISHER] Creating a new publisher with name={}", publisherRequest.getPublisherName());
        Publisher publisherMap = publisherMapper.toEntity(publisherRequest);
        Publisher savedPublisher = publisherRepository.save(publisherMap);
        log.info("[PUBLISHER] Created a new publisher with id={},name={}", savedPublisher.getPublisherId(), savedPublisher.getPublisherName());
        return publisherMapper.toResponse(savedPublisher);
    }

    @Override
    public Page<PublisherResponse> getPublishers(Pageable pageable) {
        log.info("[PUBLISHER] Getting publishers with size={}, page={}", pageable.getPageSize(), pageable.getPageNumber());
        Page<Publisher> publishers = publisherRepository.findAll(pageable);
        log.info("[PUBLISHER] Found {} publishers", publishers.getTotalElements());
        return publishers.map(publisherMapper::toResponse);
    }

    @Override
    public PublisherResponse updatePublisher(Integer id, PublisherRequest publisherRequest) {
        Publisher existedPublisher = findById(id);
        log.info("[PUBLISHER] Updating publisher with id={}, name={}", id, existedPublisher.getPublisherName());
        existedPublisher.setPublisherName(publisherRequest.getPublisherName());
        Publisher updatedPublisher = publisherRepository.save(existedPublisher);
        log.info("[PUBLISHER] Updated publisher with id={}, name={}", id, updatedPublisher.getPublisherName());
        return publisherMapper.toResponse(updatedPublisher);
    }

    @Override
    public void deletePublisher(Integer id) {
        Publisher existedPublisher = findById(id);
        log.info("[PUBLISHER] Deleting publisher with id={}", id);
        publisherRepository.delete(existedPublisher);
        log.info("[PUBLISHER] Deleted publisher with id={}", id);
    }

    @Override
    public Publisher findById(Integer id) {
        log.info("[PUBLISHER] Getting publisher with id={}", id);
        return publisherRepository.findById(id).orElseThrow(() -> {
            log.error("[PUBLISHER] Publisher with id={} not found", id);
            return new RuntimeException("Not found Publisher");
        });
    }
}
