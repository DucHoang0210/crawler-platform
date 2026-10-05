package com.dev.controller;

import com.dev.dto.NewsScanRequest;
import com.dev.dto.NewsScanResponse;
import com.dev.service.NewsMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news-monitoring")
@RequiredArgsConstructor
public class NewsMonitoringController {
    private final NewsMonitoringService newsMonitoringService;

    @PostMapping("/scan")
    public ResponseEntity<NewsScanResponse> scan(@RequestBody NewsScanRequest request) {
        return ResponseEntity.ok(newsMonitoringService.scanByKeyword(request.getKeyword()));
    }
}
