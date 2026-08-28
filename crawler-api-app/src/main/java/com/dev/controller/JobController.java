package com.dev.controller;

import com.dev.context.TenantContext;
import com.dev.job.JobManagementService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobManagementService jobManagementService;

    @PostMapping("/create")
    public ResponseEntity<String> createJob(@RequestBody CreateJobRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        jobManagementService.scheduleCrawlerJob(
                tenantId,
                request.getJobName(),
                request.getCronExpression(),
                request.getUrl(),
                request.getTitleSelector(),
                request.getPriceSelector()
        );

        return ResponseEntity.ok("Job scheduled successfully!");
    }

    @PostMapping("/pause/{jobName}")
    public ResponseEntity<String> pauseJob(@PathVariable String jobName) throws Exception {
        jobManagementService.pauseJob(TenantContext.getCurrentTenant(), jobName);
        return ResponseEntity.ok("Job paused!");
    }

    @PostMapping("/resume/{jobName}")
    public ResponseEntity<String> resumeJob(@PathVariable String jobName) throws Exception {
        jobManagementService.resumeJob(TenantContext.getCurrentTenant(), jobName);
        return ResponseEntity.ok("Job resumed!");
    }

    @Data
    public static class CreateJobRequest {
        private String jobName;
        private String cronExpression; // Ví dụ: "0 0/15 * * * ?" (mỗi 15 phút)
        private String url;
        private String titleSelector;
        private String priceSelector;
    }
}
