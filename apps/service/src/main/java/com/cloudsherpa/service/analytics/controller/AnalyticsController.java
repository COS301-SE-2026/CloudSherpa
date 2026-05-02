package com.cloudsherpa.service.analytics.controller;

import com.cloudsherpa.service.analytics.entity.NormalizedMetrics;
import com.cloudsherpa.service.analytics.service.AnalyticsPersistenceService;
import com.cloudsherpa.service.analytics.dto.MetricDTO;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController 
{
    // ! Currently only using 1 way of getting data (/metrics) because we only have 1 normalized_metrics table
    // ! We might want to change this such that the normalized_metrics table is split into 2 tables (1 for billing and 1 for usage)

    // ! We also currently have no way of initializing the foreign key environment_id in the environment_reference table
    // ! Will look into how we can do that

    private AnalyticsPersistenceService analyticsPersistenceService;

    public AnalyticsController(AnalyticsPersistenceService analyticsPersistenceService) 
    {
        this.analyticsPersistenceService = analyticsPersistenceService;
    }

    // POST request to insert a record into the database
    // Insert into the AnalyticsDB
    @PostMapping("/metrics/record") // URL: /api/analytics/metrics/record
    public String recordMetric(@RequestBody MetricDTO request) 
    {
        analyticsPersistenceService.recordMetric(request.getEnvironmentId(), request.getResourceId(), request.getServiceCategory(),
            request.getUsageAmount(), request.getUsageUnit(), request.getCostAmount(), request.getCurrency()
        );
        
        return "Metric recorded successfully";
    }

    // GET request to retrieve data
    // Using List because this function will return multiple records
    // Spring automatically serializes the list into a JSON array
    @GetMapping("/metrics/window") // URL: /api/analytics/metrics/window
    public List<NormalizedMetrics> getMetricsByTimeWindow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime, //ISO is standard date/time format
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime) 
    {
        return analyticsPersistenceService.getMetricsInTimeWindow(startTime, endTime);
    }
}