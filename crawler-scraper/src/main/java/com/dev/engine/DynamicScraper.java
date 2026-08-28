package com.dev.engine;

import com.dev.proxy.ProxyRotationManager;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DynamicScraper {

    private static final Logger log = LoggerFactory.getLogger(  DynamicScraper.class);

    @Autowired
    private ProxyRotationManager proxyRotationManager;

    /**
     * Mở Chrome ẩn danh (Headless), render toàn bộ JS rồi trả về HTML đầy đủ
     */
    public String scrape(String url, int waitTimeMs) {
       return scrape(url, waitTimeMs, null);
    }

    public String scrape(String url, int waitTimeMs, Map<String, String> selectors) {
       try (Playwright playwright = Playwright.create()) {

           BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                   .setHeadless(true);

           String proxyStr = proxyRotationManager.getNextProxy();
           if (proxyStr != null) {
               launchOptions.setProxy(new Proxy(proxyStr));
           }

           try (Browser browser = playwright.chromium().launch(launchOptions);
                BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                        .setViewportSize(1920, 1080));
                Page page = context.newPage()) {

               page.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

               log.info("Đang điều hướng tới URL bằng Playwright: {}", url);
               page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
               page.waitForLoadState();

               if (selectors != null && !selectors.isEmpty()) {
                   for (String selector : selectors.values()) {
                       if (selector == null || selector.isBlank()) {
                           continue;
                       }
                       try {
                           page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(10000));
                       } catch (Exception ignored) {
                           log.warn("Selector {} không xuất hiện trên {}", selector, url);
                       }
                   }
               }

               page.evaluate("window.scrollTo(0, document.body.scrollHeight / 2)");
               page.waitForTimeout(10000);
               page.evaluate("window.scrollTo(0, document.body.scrollHeight)");

               int finalWait = waitTimeMs > 0 ? waitTimeMs : 2000;
               page.waitForTimeout(finalWait);
               return page.content();
           }
       } catch (Exception e) {
           log.error("Lỗi khi cào động URL {}: {}", url, e.getMessage());
           throw new RuntimeException("Cào động thất bại: " + e.getMessage(), e);
       }
    }
}
