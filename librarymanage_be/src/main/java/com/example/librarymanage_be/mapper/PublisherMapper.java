package com.example.librarymanage_be.mapper;

import com.example.librarymanage_be.dto.request.PublisherRequest;
import com.example.librarymanage_be.dto.response.PublisherResponse;
import com.example.librarymanage_be.entity.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PublisherMapper {
    @Mapping(source = "publisherName" ,target = "publisherName")
    Publisher toEntity(PublisherRequest publisherRequest);

    @Mapping(source = "publisherId" ,target = "publisherId")
    @Mapping(source = "publisherName" ,target = "publisherName")
    PublisherResponse toResponse(Publisher publisher);
}
