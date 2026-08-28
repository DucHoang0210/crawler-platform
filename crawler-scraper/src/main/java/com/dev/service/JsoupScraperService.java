package com.dev.service;

import com.dev.entity.ScraperResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class JsoupScraperService {

    public ScraperResponse scrape(String url, String titleCssQuery, String priceCssQuery) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get();

            String title = doc.select(titleCssQuery).text();
            String priceRaw = doc.select(priceCssQuery).text().replaceAll("[^0-9]", "");
            BigDecimal price = priceRaw.isBlank() ? null : new BigDecimal(priceRaw);

            return ScraperResponse.builder()
                    .url(url)
                    .title(title)
                    .currentPrice(price)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Jsoup scraping failed for URL: " + url, e);
        }
    }
}
