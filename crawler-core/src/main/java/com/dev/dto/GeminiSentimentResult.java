package com.dev.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeminiSentimentResult {
    private String label;
    private double score;
    private String reason;
}
