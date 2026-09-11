package com.xin898.trademonitor.api;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/monitor")
public class MonitorController {

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "service", "trade-monitor",
                "role", "business-observability",
                "criticalTradingPath", false,
                "timestamp", Instant.now().toString());
    }
}
