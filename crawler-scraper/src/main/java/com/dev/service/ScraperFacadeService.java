package com.dev.service;

import com.dev.engine.DynamicScraper;
import com.dev.engine.StaticScraper;
import com.dev.domain.DomainConfig;
import com.dev.parser.HTMLParser;
import com.dev.repository.DomainConfigRepository;
import com.dev.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScraperFacadeService {
    private static final Logger log = LoggerFactory.getLogger(ScraperFacadeService.class);

    @Autowired
    private StaticScraper staticScraper;

    @Autowired
    private DynamicScraper dynamicScraper;

    @Autowired
    private HTMLParser htmlParser;

    @Autowired
    private DomainConfigRepository domainConfigRepository;

    // Cache trong bộ nhớ tạm để giảm tải query DB
    private final Map<String, DomainConfig> configCache = new ConcurrentHashMap<>();

    /**
     * Phương thức cào tự động nhận diện Dynamic/Static dựa vào DB
     */
    public Map<String, String> executeScraping(String url) {
        String domain = UrlUtils.extractDomain(url);
        DomainConfig config = getDomainConfig(domain);
        configCache.remove(domain);
        config = getDomainConfig(domain);

        Map<String, String> effectiveSelectors = buildSelectorsFromConfig(config);
        log.info("Scrape URL: {}, domain: {}, isDynamic: {}, selectors: {}",
                url, domain, config.isDynamic(), effectiveSelectors);

        String html;
        if (config.isDynamic()) {
            int timeout = config.getTimeoutMs() != null ? config.getTimeoutMs() : 5000;
            html = dynamicScraper.scrape(url, timeout, effectiveSelectors);
        } else {
            html = staticScraper.scrape(url);
        }

        return htmlParser.parseData(html, effectiveSelectors);
    }

    private Map<String, String> buildSelectorsFromConfig(DomainConfig config) {
        Map<String, String> selectors = new LinkedHashMap<>();
        if (hasSelector(config.getPriceSelector())) {
            selectors.put("price", config.getPriceSelector());
        }
        if (hasSelector(config.getProductNameSelector())) {
            selectors.put("productName", config.getProductNameSelector());
        }
        if (hasSelector(config.getTitleSelector())) {
            selectors.put("title", config.getTitleSelector());
        }
        if (hasSelector(config.getDescriptionSelector())) {
            selectors.put("description", config.getDescriptionSelector());
        }
        return selectors;
    }

    private boolean hasSelector(String rawSelector) {
        if (rawSelector == null) {
            return false;
        }
        String[] candidates = rawSelector.split("\\|\\|");
        for (String candidate : candidates) {
            if (candidate != null && !candidate.trim().isBlank()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lấy cấu hình từ Cache -> DB -> Mặc định (Static)
     */
    private DomainConfig getDomainConfig(String domain) {
        // 1. Kiểm tra Cache
        if (configCache.containsKey(domain)) {
            return configCache.get(domain);
        }

        // 2. Query từ Database
        DomainConfig config = domainConfigRepository.findByDomainIgnoreCase(domain)
                .orElseGet(() -> {
                    // Mặc định nếu domain chưa đăng ký trong DB -> Coi như Web tĩnh (Static)
                    DomainConfig defaultConfig = new DomainConfig();
                    defaultConfig.setDomain(domain);
                    defaultConfig.setDynamic(true);
                    defaultConfig.setTimeoutMs(12000);
                    return defaultConfig;
                });

        // 3. Lưu vào Cache
        configCache.put(domain, config);
        return config;
    }

    /**
     * Hàm dùng để xoá Cache khi có cập nhật cấu hình mới trong DB
     */
    public void clearConfigCache() {
        configCache.clear();
    }
}