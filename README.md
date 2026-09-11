# TradeMonitor

TradeMonitor is the **business-observability and reconciliation SCS** for the trading platform. It monitors StockTrader and CrypTrader without participating in the critical trading decision or execution path.

Its failure must **not stop trading**. The system is primarily read-oriented and receives trading state through explicit events/APIs rather than directly reading trader databases.


## 🗺️ Development Roadmap

The platform will be delivered incrementally. Each phase should produce a runnable vertical slice before the next major capability is added.

### Phase 1 — TradingAgents Production-Ready

- [ ] Run the TradingAgents workflow end-to-end
- [ ] Expose a stable Analysis API
- [ ] Finalize and version `AnalysisSignal v1`
- [ ] Persist analysis jobs/results across restarts
- [ ] Add health checks, metrics and failure visibility
- [ ] Deploy TradingAgents to a reachable environment
- [ ] Validate signal freshness through `dataTimestamp` and `validUntil`
- [ ] Ensure TradingAgents remains decision support only and never submits broker/exchange orders

**Exit criterion:** an external consumer can reliably request/query analysis and consume a versioned AnalysisSignal.

### Phase 2 — TradeMonitor + TradingAgents

- [ ] Connect TradeMonitor to TradingAgents through explicit API/events
- [ ] Build an independent TradeMonitor read model
- [ ] Show analysis jobs and signal history
- [ ] Show analysis latency and failures
- [ ] Show model/version information
- [ ] Add LLM token/cost monitoring where available
- [ ] Add initial alerting
- [ ] Verify TradeMonitor failure does not affect TradingAgents

**Exit criterion:** TradeMonitor provides an operational view of AI analysis without reading TradingAgents storage directly.

### Phase 3 — StockTrader + Alpaca Paper Trading

- [ ] Consume `AnalysisSignal v1`
- [ ] Implement Strategy → `TradeIntent`
- [ ] Implement deterministic pre-trade Risk checks
- [ ] Implement the Order Management state machine
- [ ] Implement Portfolio/position state
- [ ] Implement `ExecutionGateway` and Alpaca Paper Trading adapter
- [ ] Execute BUY/SELL flows in the Alpaca test environment
- [ ] Handle partial fills, rejects and cancellations
- [ ] Implement idempotency and duplicate-signal protection
- [ ] Implement timeout/unknown-order-state recovery
- [ ] Implement Alpaca reconciliation
- [ ] Publish versioned stock trading events
- [ ] Validate stateless multi-instance behavior and horizontal scaling

**Exit criterion:** a valid TradingAgents signal can pass Strategy → Risk → OMS → Alpaca and update the owned portfolio safely.

### Phase 3.1 — TradeMonitor Stock Dashboard

- [ ] Consume StockTrader order/execution/position/risk events
- [ ] Add stock order lifecycle view
- [ ] Add executions/fills view
- [ ] Add positions and PnL
- [ ] Add exposure and risk-rejection view
- [ ] Add Alpaca connectivity status
- [ ] Add StockTrader ↔ Alpaca reconciliation status
- [ ] Add business alerts

**Exit criterion:** stock trading can be monitored end-to-end without TradeMonitor accessing the StockTrader database.

### Phase 4 — CrypTrader + Binance

- [ ] Consume `AnalysisSignal v1`
- [ ] Implement Crypto Strategy → Risk → OMS → Portfolio flow
- [ ] Implement Binance market-data adapter
- [ ] Implement Binance execution adapter
- [ ] Support 24/7 market operation
- [ ] Handle WebSocket reconnect/resubscribe
- [ ] Handle sequence/order-book consistency where required
- [ ] Handle symbol precision, lot size and tick size
- [ ] Handle Binance rate limits and exchange errors
- [ ] Implement idempotency and safe retries
- [ ] Implement Binance reconciliation
- [ ] Publish versioned crypto trading events
- [ ] Validate stateless multi-instance behavior and horizontal scaling

**Exit criterion:** CrypTrader can safely execute and reconcile automated trades in the selected Binance test environment.

### Phase 4.1 — TradeMonitor Crypto Dashboard

- [ ] Consume CrypTrader order/execution/position/risk events
- [ ] Add crypto order lifecycle view
- [ ] Add executions/fills view
- [ ] Add balances, positions and PnL
- [ ] Add exposure and risk-rejection view
- [ ] Add Binance/WebSocket connectivity status
- [ ] Add CrypTrader ↔ Binance reconciliation status
- [ ] Add crypto-specific business alerts

**Exit criterion:** crypto trading can be monitored end-to-end from the shared TradeMonitor platform.

### Phase 5 — Architecture Hardening

- [ ] Run multiple StockTrader instances
- [ ] Run multiple CrypTrader instances
- [ ] Run multiple TradeMonitor instances
- [ ] Verify no duplicate trades under concurrent processing
- [ ] Verify safe concurrent portfolio/order updates
- [ ] Test Kafka/event replay and duplicate delivery
- [ ] Test database/process restart recovery
- [ ] Test Alpaca timeout/failure scenarios
- [ ] Test Binance disconnect/recovery scenarios
- [ ] Add/complete OpenTelemetry tracing
- [ ] Add Prometheus metrics and Grafana dashboards
- [ ] Measure p95/p99 analysis and order-processing latency
- [ ] Monitor Kafka lag and database connection pools
- [ ] Review authentication, authorization and secret management
- [ ] Run resilience/failure tests and document results

**Exit criterion:** the platform demonstrates horizontal scalability, failure isolation, recovery, observability and operational readiness.

### Roadmap overview

```text
Phase 1   TradingAgents Production-Ready
                    ↓
Phase 2   TradeMonitor + AI Monitoring
                    ↓
Phase 3   StockTrader + Alpaca Paper Trading
                    ↓
Phase 3.1 TradeMonitor Stock Dashboard
                    ↓
Phase 4   CrypTrader + Binance
                    ↓
Phase 4.1 TradeMonitor Crypto Dashboard
                    ↓
Phase 5   Scalability / Resilience / Observability Hardening
```


## Responsibilities

- monitor orders, fills, rejects and cancellations
- monitor stock/crypto positions and PnL
- aggregate exposure and risk events
- detect abnormal trading/business conditions
- provide alerts for operational and business incidents
- reconcile internal trader state with broker/exchange state
- provide a unified stock + crypto monitoring UI
- retain an auditable monitoring history

## Overall architecture

```text
StockTrader                         CrypTrader
    │                                   │
    │ order.*                           │ order.*
    │ execution.*                       │ execution.*
    │ position.*                        │ position.*
    │ risk.*                            │ risk.*
    │ health.*                          │ exchange.*
    └──────────────┐       ┌────────────┘
                   ▼       ▼
                 Event Bus
                    │
                    ▼
              event-ingestion
                    │
        ┌───────────┼────────────┐
        ▼           ▼            ▼
trade-observability │      risk-monitoring
                    │
          portfolio-monitoring
                    │
        ┌───────────┴───────────┐
        ▼                       ▼
     alerting              reconciliation
        │                       │
        └──────────┬────────────┘
                   ▼
             Monitoring UI
```

## Modules

| Module | Responsibility |
|---|---|
| `event-ingestion` | Consume/version/deduplicate StockTrader and CrypTrader events |
| `trade-observability` | Order/fill/reject/cancel state and execution latency |
| `portfolio-monitoring` | Positions, balances, PnL and exposure views |
| `risk-monitoring` | Risk rejections, thresholds and abnormal exposure |
| `alerting` | Business/operational alerts and notification routing |
| `reconciliation` | Compare internal trader state with broker/exchange state |
| `platform` | Persistence, event infrastructure, security and telemetry |

## Reconciliation examples

```text
StockTrader: AAPL position = 100
Alpaca:      AAPL position = 90
                    ↓
          reconciliation mismatch
                    ↓
                  alert
```

```text
CrypTrader: BTC position = 0.50
Binance:    BTC position = 0.48
                    ↓
          reconciliation mismatch
                    ↓
                  alert
```

## Monitoring boundary

TradeMonitor focuses primarily on **business/trading observability**:

- orders and executions
- positions and PnL
- exposure and risk rejects
- sequence/reconciliation problems
- broker/exchange connectivity from a trading perspective

Low-level infrastructure metrics such as CPU, JVM GC, memory and Kafka consumer lag remain available through the platform observability stack (for example Prometheus/Grafana), while TradeMonitor presents the business-level operational picture.

## SCS principles

- no direct reads from StockTrader/CrypTrader databases
- consume versioned events or explicit read APIs
- tolerate duplicate and out-of-order events
- monitoring failure must not block trading
- maintain its own read model optimized for monitoring
- reconciliation and alerts are explicit domain capabilities

## Platform context

```text
                         TradingAgents
                        /             \
               AnalysisSignal     AnalysisSignal
                     /                 \
              StockTrader          CrypTrader
                  │                    │
                Alpaca              Binance
                  │                    │
                  └──── events ────────┘
                           │
                           ▼
                      TradeMonitor
```
