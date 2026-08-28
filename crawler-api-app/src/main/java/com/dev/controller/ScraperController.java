package com.dev.controller;

import com.dev.dto.ScrapeRequest;
import com.dev.service.ScraperFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/scraper")
public class ScraperController {

    @Autowired
    private ScraperFacadeService scraperFacadeService;

    @PostMapping
    public ResponseEntity<?> testScrape(@RequestBody ScrapeRequest request) {
        // Cào dữ liệu theo selector đã lưu trong DB theo domain
        Map<String, String> result = scraperFacadeService.executeScraping(request.getUrl());
        System.out.println("Scraping result: " + result);

        // Trả về JSON kết quả
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "targetUrl", request.getUrl(),
                "extractedData", result.entrySet().stream()
                        .filter(entry -> entry.getValue() != null)
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        ))
        ));
    }
}