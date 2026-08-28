package com.dev.service;

import com.dev.entity.ScraperResponse;
import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PlaywrightScraperService {

    public ScraperResponse scrape(String url, String titleSelector, String priceSelector) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            // Navigate và đợi network nhàn rỗi
            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
            page.waitForSelector(titleSelector);

            String title = page.querySelector(titleSelector).innerText();
            String priceRaw = page.querySelector(priceSelector).innerText().replaceAll("[^0-9]", "");
            BigDecimal price = priceRaw.isBlank() ? null : new BigDecimal(priceRaw);

            browser.close();

            return ScraperResponse.builder()
                    .url(url)
                    .title(title)
                    .currentPrice(price)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Playwright scraping failed for URL: " + url, e);
        }
    }
}
