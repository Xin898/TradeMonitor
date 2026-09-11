package com.xin898.trademonitor.event;

import java.math.BigDecimal;
import java.time.Instant;

public record TradingEvent(
        String eventId,
        int schemaVersion,
        String source,
        String type,
        String instrument,
        String orderId,
        BigDecimal quantity,
        BigDecimal price,
        Instant occurredAt) {
}
