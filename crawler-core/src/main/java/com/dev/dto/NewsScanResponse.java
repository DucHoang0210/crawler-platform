package com.dev.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NewsScanResponse {
    private String keyword;
    private int matchedCount;
    private List<NewsScanResultItem> matches;
}
