package com.example.librarymanage_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublisherRequest {
    @NotBlank(message = "Không được trống")
    @Size(max = 255 , message = "Không được quá 255 kí tự")
    private String publisherName;

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }
}
