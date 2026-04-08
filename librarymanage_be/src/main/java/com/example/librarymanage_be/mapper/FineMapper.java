package com.example.librarymanage_be.mapper;

import com.example.librarymanage_be.dto.request.FineRequest;
import com.example.librarymanage_be.dto.response.FineResponse;
import com.example.librarymanage_be.Entity.Fine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FineMapper {
    @Mapping(target = "fineId", ignore = true)
    @Mapping(target = "borrowDetail", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Fine toEntity(FineRequest fineRequest);

    @Mapping(source = "borrowDetail.id", target = "borrowDetailId")
    FineResponse toResponse(Fine fine);
}


