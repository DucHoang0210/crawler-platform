package com.dev.job;

import com.dev.context.TenantContext;
import com.dev.entity.ScraperResponse;
import com.dev.service.JsoupScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;

import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicCrawlerJob extends QuartzJobBean {

    private final JsoupScraperService jsoupScraperService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();

        String tenantId = dataMap.getString("tenantId");
        String url = dataMap.getString("url");
        String titleSelector = dataMap.getString("titleSelector");
        String priceSelector = dataMap.getString("priceSelector");

        // Thiết lập TenantContext cho luồng chạy ngầm của Quartz
        TenantContext.setCurrentTenant(tenantId);
        log.info("Executing Job for Tenant: [{}] - URL: {}", tenantId, url);

        try {
            ScraperResponse result = jsoupScraperService.scrape(url, titleSelector, priceSelector);
            log.info("Scraped Successfully: Title = {}, Price = {}", result.getTitle(), result.getCurrentPrice());

            // TODO (Tuần 3): Đẩy result vào Kafka Producer!
        } catch (Exception e) {
            log.error("Error executing crawler job for URL: {}", url, e);
        } finally {
            TenantContext.clear();
        }
    }
}