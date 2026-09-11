package com.xin898.trademonitor.event;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EventIngestionService {

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = {"stock-trading-events", "crypto-trading-events"}, groupId = "trade-monitor")
    public void onEvent(TradingEvent event) {
        // Foundation implementation only.
        // Replace in-memory idempotency with a durable read-model/event-inbox in the next epic.
        if (!processedEventIds.add(event.eventId())) {
            return;
        }

        switch (event.type()) {
            case "ORDER_FILLED", "ORDER_REJECTED", "POSITION_UPDATED", "RISK_REJECTED" ->
                    project(event);
            default -> {
                // Unknown event types are ignored until a compatible schema handler is available.
            }
        }
    }

    private void project(TradingEvent event) {
        // TODO persist monitoring read models and trigger alert/reconciliation workflows.
    }
}
