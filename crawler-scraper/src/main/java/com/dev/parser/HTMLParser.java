package com.dev.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HTMLParser {

    /**
     * Parse HTML thô dựa trên Map các selector (Field Name -> CSS Selector)
     */
    public Map<String, String> parseData(String htmlContent, Map<String, String> selectors) {
        Map<String, String> extractedData = new HashMap<>();

        if (htmlContent == null || htmlContent.isBlank() || selectors == null || selectors.isEmpty()) {
            return extractedData;
        }

        Document doc = Jsoup.parse(htmlContent);

        for (Map.Entry<String, String> entry : selectors.entrySet()) {
            String fieldName = entry.getKey();
            String cssSelector = entry.getValue();

            String value = null;
            if (cssSelector != null && !cssSelector.isBlank()) {
                String[] candidates = cssSelector.split("\\|\\|");
                for (String candidate : candidates) {
                    String selector = candidate == null ? "" : candidate.trim();
                    if (selector.isBlank()) {
                        continue;
                    }
                    Element element = doc.selectFirst(selector);
                    if (element != null) {
                        value = firstMeaningfulText(element);
                        if (value != null && !value.isBlank()) {
                            break;
                        }
                    }
                }
            }

            extractedData.put(fieldName, value);
        }

        return extractedData;
    }

    private String firstMeaningfulText(Element element) {
        if (element == null) {
            return null;
        }

        String value = element.text();
        if (value == null || value.isBlank()) {
            value = element.attr("content");
        }
        if (value == null || value.isBlank()) {
            value = element.attr("value");
        }
        if (value == null || value.isBlank()) {
            value = element.attr("data-name");
        }
        if (value == null || value.isBlank()) {
            value = element.attr("data-price");
        }
        if (value == null || value.isBlank()) {
            value = element.attr("title");
        }
        if (value == null || value.isBlank()) {
            value = element.attr("alt");
        }

        return value == null ? null : value.trim();
    }
}