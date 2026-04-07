package com.example.librarymanage_be.controller;

import com.example.librarymanage_be.dto.request.FineRequest;
import com.example.librarymanage_be.dto.response.FineResponse;
import com.example.librarymanage_be.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {
    private final FineService fineService;
    @GetMapping()
    public ResponseEntity<Page<FineResponse>> getFines(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "5") int size){
        Pageable pageable =  PageRequest.of(page, size);
        return ResponseEntity.ok(fineService.getFines(pageable));

    }

    @PostMapping("/create")
    public ResponseEntity<FineResponse> create(@RequestBody FineRequest fineRequest) {
        FineResponse fineResponse = fineService.create(fineRequest);
        return ResponseEntity.ok(fineResponse);
    }
    @PutMapping("/pay/{fineId}")
    public ResponseEntity<FineResponse> pay(@PathVariable Integer fineId) {
        fineService.pay(fineId);
        return ResponseEntity.ok().build();
    }
}
