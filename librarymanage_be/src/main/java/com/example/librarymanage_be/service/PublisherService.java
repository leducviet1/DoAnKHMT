package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.PublisherRequest;
import com.example.librarymanage_be.dto.response.PublisherResponse;
import com.example.librarymanage_be.entity.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublisherService {
    PublisherResponse createPublisher(PublisherRequest publisherRequest);

    Page<PublisherResponse> getPublishers(Pageable pageable);

    PublisherResponse updatePublisher(Integer id, PublisherRequest publisherRequest);

    void deletePublisher(Integer id);

    Publisher findById(Integer id);
}
