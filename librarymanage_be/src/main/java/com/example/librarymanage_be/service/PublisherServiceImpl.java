package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.PublisherRequest;
import com.example.librarymanage_be.dto.response.PublisherResponse;
import com.example.librarymanage_be.mapper.PublisherMapper;
import com.example.librarymanage_be.model.Publisher;
import com.example.librarymanage_be.repo.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService{
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

//    private final PublisherMapper publisherMapper;
//    public PublisherServiceImpl(PublisherRepository publisherRepository) {
//        this.publisherRepository = publisherRepository;
//    }
//
    @Override
    public PublisherResponse createPublisher(PublisherRequest publisherRequest) {
        Publisher publisherMap = publisherMapper.toEntity(publisherRequest);
        Publisher savedPublisher = publisherRepository.save(publisherMap);
        return publisherMapper.toResponse(savedPublisher);
    }

    @Override
    public Page<PublisherResponse> getPublishers(Pageable pageable) {
        Page<Publisher> publishers = publisherRepository.findAll(pageable);
        return publishers.map(publisherMapper::toResponse);
    }

    @Override
    public PublisherResponse updatePublisher(Integer id,PublisherRequest publisherRequest) {
        Publisher existedPublisher = findById(id);
        existedPublisher.setPublisherName(publisherRequest.getPublisherName());
        Publisher updatedPublisher = publisherRepository.save(existedPublisher);
        return publisherMapper.toResponse(updatedPublisher);
    }

    @Override
    public void deletePublisher(Integer id) {
        Publisher existedPublisher = findById(id);
        publisherRepository.delete(existedPublisher);
    }

    @Override
    public Publisher findById(Integer id) {
        return publisherRepository.findById(id).orElseThrow(()
                -> new RuntimeException("Not found Publisher"));
    }

}
