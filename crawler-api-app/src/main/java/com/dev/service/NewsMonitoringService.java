package com.dev.service;

import com.dev.domain.DomainConfig;
import com.dev.dto.GeminiSentimentResult;
import com.dev.dto.NewsScanResponse;
import com.dev.dto.NewsScanResultItem;
import com.dev.repository.DomainConfigRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsMonitoringService {
    private static final Set<Long> ALLOWED_DOMAIN_IDS = Set.of(3L, 4L);

    private final DomainConfigRepository domainConfigRepository;
    private final ScraperFacadeService scraperFacadeService;
    private final GeminiSentimentService geminiSentimentService;

    public NewsScanResponse scanByKeyword(String keywordInput) {
        if (keywordInput == null || keywordInput.isBlank()) {
            throw new IllegalArgumentException("keyword is required.");
        }

        String keyword = keywordInput.trim();
        List<DomainConfig> domains = domainConfigRepository.findAll()
                .stream()
                .filter(domain -> domain.getId() != null && ALLOWED_DOMAIN_IDS.contains(domain.getId()))
                .collect(Collectors.toList());
        List<NewsScanResultItem> matches = new ArrayList<>();

        for (DomainConfig config : domains) {
            String domain = config.getDomain();
            if (domain == null || domain.isBlank()) {
                continue;
            }

            String sourceUrl = "https://" + domain;
            Map<String, String> extracted = safeExtract(sourceUrl);
            String title = extracted.getOrDefault("title", "");
            String description = extracted.getOrDefault("description", "");
            String pageText = extractPageText(sourceUrl, title, description);

            if (!containsKeyword(pageText, keyword)) {
                continue;
            }

            GeminiSentimentResult sentiment = geminiSentimentService.analyzeSentiment(keyword, title, description, sourceUrl);
            matches.add(NewsScanResultItem.builder()
                    .sourceDomain(domain)
                    .sourceUrl(sourceUrl)
                    .title(title)
                    .description(description)
                    .sentimentLabel(sentiment.getLabel())
                    .sentimentScore(sentiment.getScore())
                    .reason(sentiment.getReason())
                    .build());
        }

        return NewsScanResponse.builder()
                .keyword(keyword)
                .matchedCount(matches.size())
                .matches(matches)
                .build();
    }

    private boolean containsKeyword(String text, String keyword) {
        if (text == null || text.isBlank() || keyword == null || keyword.isBlank()) {
            return false;
        }

        String normalizedText = normalizeText(text);
        String normalizedKeyword = normalizeText(keyword);
        return normalizedText.contains(normalizedKeyword);
    }

    private String normalizeText(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}+", "");
        normalized = normalized.toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^\\p{L}\\p{N}\\s]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private String extractPageText(String sourceUrl, String title, String description) {
        try {
            Document doc = Jsoup.connect(sourceUrl).timeout(10000).get();
            String text = doc.text();
            if (text == null || text.isBlank()) {
                text = title + " " + description;
            }
            return text;
        } catch (Exception e) {
            return title + " " + description;
        }
    }

    private Map<String, String> safeExtract(String sourceUrl) {
        try {
            Map<String, String> extracted = scraperFacadeService.executeScraping(sourceUrl);
            if (extracted == null) {
                return fallbackExtract(sourceUrl);
            }
            return extracted;
        } catch (Exception e) {
            return fallbackExtract(sourceUrl);
        }
    }

    private Map<String, String> fallbackExtract(String sourceUrl) {
        try {
            Document doc = Jsoup.connect(sourceUrl).timeout(10000).get();
            String title = doc.title();
            String description = doc.selectFirst("meta[name=description]") != null
                    ? doc.selectFirst("meta[name=description]").attr("content")
                    : "";
            Map<String, String> data = new LinkedHashMap<>();
            data.put("title", title);
            data.put("description", description);
            return data;
        } catch (Exception ex) {
            return Map.of("title", "", "description", "");
        }
    }
}
