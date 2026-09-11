# TradeMonitor Architecture

## SCS boundary

TradeMonitor is an independently deployable **business-observability and reconciliation SCS**. It is deliberately outside the critical order-decision/execution path.

## NFRs and architecture principles

| ID | Requirement | Architecture consequence |
|---|---|---|
| NFR-M01 | Trading independence | TradeMonitor failure must not block StockTrader or CrypTrader. |
| NFR-M02 | Independent data ownership | TradeMonitor owns its monitoring/read-model database and never directly reads Trader databases. |
| NFR-M03 | Loose coupling | Integration uses versioned trading events and explicit read/reconciliation APIs. |
| NFR-M04 | Horizontal scalability | Event consumers/read APIs can scale independently; event handling must be idempotent. |
| NFR-M05 | Eventual consistency | Monitoring views may lag the source Trader; freshness and last-updated timestamps must be visible. |
| NFR-M06 | Replay/rebuild | Read models should be recoverable/rebuildable from durable event sources where available. |
| NFR-M07 | Observability | Monitor its own ingestion lag, processing failures, alert delivery and reconciliation status. |

## Data flow

```text
StockTrader                     CrypTrader
    │                               │
    └──── versioned events ─────────┘
                  │
                  ▼
            event-ingestion
                  │
           own read models
                  │
     ┌────────────┼────────────┐
     ▼            ▼            ▼
 trade view   risk view   portfolio view
     │            │            │
     └────────────┼────────────┘
                  ▼
       alerting / reconciliation
                  ▼
            Monitoring UI
```

## Data ownership rule

```text
FORBIDDEN:
TradeMonitor ─────► StockTrader DB
TradeMonitor ─────► CrypTrader DB

ALLOWED:
Trader ──versioned event──► TradeMonitor
TradeMonitor ──explicit API──► Trader / broker-exchange reconciliation endpoint
```

This keeps monitoring independently deployable and prevents schema coupling.

## Scaling correctness

Multiple consumers must safely tolerate:

- duplicate events
- out-of-order events
- replayed events
- temporary event-bus outages
- consumer restarts
- delayed monitoring state

Monitoring is not the source of truth for order/position state; the owning Trader remains authoritative.
