package com.dev.dto;

import lombok.Data;
import java.util.List;

@Data
public class ScrapeRequest {
    private String url;

    // Danh sách proxy muốn test (tùy chọn)
    private List<String> proxies;
}