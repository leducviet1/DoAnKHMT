package com.example.librarymanage_be.controller;

import com.example.librarymanage_be.dto.request.PublisherRequest;
import com.example.librarymanage_be.dto.response.PublisherResponse;
import com.example.librarymanage_be.entity.Publisher;
import com.example.librarymanage_be.service.PublisherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublisherController {
    private final PublisherService publisherService;

//    public PublisherController(PublisherService publisherService) {
//        this.publisherService = publisherService;
//    }
    @GetMapping
    public Page<PublisherResponse> getPublishers(@RequestParam(defaultValue = "0")int page,
                                          @RequestParam(defaultValue = "5")int size) {
        Pageable pageable = PageRequest.of(page, size);
        return publisherService.getPublishers(pageable);
    }

    @GetMapping("/{id}")
    public Publisher findById(@PathVariable int id) {
        return publisherService.findById(id);
    }

    @PostMapping("/create")
    public PublisherResponse create(@RequestBody @Valid PublisherRequest publisherRequest) {
        return publisherService.createPublisher(publisherRequest);
    }
    @PutMapping("/update/{id}")
    public PublisherResponse update(@PathVariable Integer id,
                            @Valid
                            @RequestBody PublisherRequest publisherRequest) {
        return publisherService.updatePublisher(id, publisherRequest);
    }
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        publisherService.deletePublisher(id);
    }
}
