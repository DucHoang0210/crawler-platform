package com.dev.engine;

import com.dev.proxy.ProxyRotationManager;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StaticScraper {

    private static final Logger log = LoggerFactory.getLogger(StaticScraper.class);

    @Autowired
    private ProxyRotationManager proxyRotationManager;

    /**
     * Cào HTML của trang web bằng Jsoup
     */
    public String scrape(String url) {
        try {
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .followRedirects(true);

            // Cấu hình Proxy nếu có
            String proxyStr = proxyRotationManager.getNextProxy();
            if (proxyStr != null) {
                String[] parts = proxyStr.replace("http://", "").split(":");
                connection.proxy(parts[0], Integer.parseInt(parts[1]));
            }

            Document doc = connection.get();
            return doc.html();
        } catch (IOException e) {
            log.error("Lỗi khi cào tĩnh URL {}: {}", url, e.getMessage());
            throw new RuntimeException("Cào tĩnh thất bại: " + e.getMessage(), e);
        }
    }
}