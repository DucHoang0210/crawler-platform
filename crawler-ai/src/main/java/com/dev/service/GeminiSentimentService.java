package com.dev.service;

import com.dev.dto.GeminiSentimentResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiSentimentService {
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    public GeminiSentimentResult analyzeSentiment(String keyword, String title, String description, String sourceUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing GEMINI API key. Please set GEMINI_API_KEY or gemini.api.key.");
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        String prompt = """
                Bạn là hệ thống phân tích cảm xúc bài báo tiếng Việt.
                Dựa trên thông tin đầu vào, hãy phân tích bài báo có nhắc đến từ khóa hay không và cảm xúc chung về thực thể đó.

                Từ khóa cần theo dõi: %s
                URL: %s
                Tiêu đề: %s
                Mô tả/Tóm tắt: %s

                Chỉ trả về JSON hợp lệ, không thêm markdown, theo schema:
                {
                  "label": "positive|negative|neutral",
                  "score": 0.0,
                  "reason": "giải thích ngắn tiếng Việt"
                }
                """.formatted(keyword, sourceUrl, safe(title), safe(description));

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{Map.of("text", prompt)})
                },
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "maxOutputTokens", 300,
                        "responseMimeType", "application/json"
                )
        );

        String raw = RestClient.create()
                .post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new IllegalStateException("Gemini returned empty candidates.");
            }
            String json = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            JsonNode parsed = objectMapper.readTree(json);
            String label = normalizeLabel(parsed.path("label").asText("neutral"));
            double score = parsed.path("score").asDouble(0.5d);
            String reason = parsed.path("reason").asText("Không có mô tả.");
            return GeminiSentimentResult.builder()
                    .label(label)
                    .score(score)
                    .reason(reason)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse Gemini sentiment response: " + e.getMessage(), e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String normalizeLabel(String label) {
        String normalized = label == null ? "neutral" : label.trim().toLowerCase();
        if (!normalized.equals("positive") && !normalized.equals("negative") && !normalized.equals("neutral")) {
            return "neutral";
        }
        return normalized;
    }
}
 