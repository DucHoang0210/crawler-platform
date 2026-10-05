package com.dev.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsScanResultItem {
    private String sourceDomain;
    private String sourceUrl;
    private String title;
    private String description;
    private String sentimentLabel;
    private double sentimentScore;
    private String reason;
}
