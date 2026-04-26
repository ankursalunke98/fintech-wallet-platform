# Fintech Wallet Platform

A production-grade digital wallet system built with Spring Boot 3 microservices, PostgreSQL, Kafka, and Redis. Implements double-entry ledger semantics, idempotent money transfers, and event-driven notifications — patterns used by real fintechs like Razorpay and Stripe.

> 🚧 **Active development** — this project is being built feature-by-feature over a 14-day learning sprint. See the [Roadmap](#roadmap) for current state.

## Why this project exists

Most "wallet" tutorials store balance as a single mutable column and call it done. That's not how real fintech systems work, and it falls apart the moment you have concurrent writes, a failed network call, or a regulator asking for an audit trail.

This project implements the patterns that actually run in production:

- **Double-entry ledger** — every transaction is an atomic debit + credit, never a balance update
- **Idempotency keys** — clients can safely retry transfers without double-charging
- **Outbox pattern** — distributed events without two-phase-commit pain
- **Optimistic locking + transactional integrity** — concurrent transfers stay consistent
- **Event-driven notifications** — Kafka decouples notification delivery from transaction commit

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.x, Spring Cloud Gateway |
| Persistence | PostgreSQL 16, Spring Data JPA, Flyway migrations |
| Messaging | Apache Kafka |
| Cache | Redis 7 |
| Resilience | Resilience4j (circuit breaker, retry, rate limiter) |
| Auth | JWT |
| Observability | Spring Actuator, Prometheus metrics |
| Testing | JUnit 5, Mockito, Testcontainers |
| CI/CD | GitHub Actions |
| Packaging | Docker, Docker Compose |

## Architecture

*Architecture diagram coming on Day 12.*

Services:
- **User Service** — registration, JWT auth, profile management
- **Wallet Service** — accounts, ledger, transfers, balance queries
- **Notification Service** — Kafka consumer, email/SMS dispatch
- **API Gateway** — single entry point, JWT validation, rate limiting

## Running locally

```bash
git clone git@github.com:ankursalunke98/fintech-wallet-platform.git
cd fintech-wallet-platform
docker compose up -d
# More services coming as the project develops
```

## Roadmap

- [x] Day 1 — Postgres in Docker, project scaffolding, GitHub workflow
- [ ] Day 2 — User Service: registration, JWT login, Flyway migrations
- [ ] Day 3 — Wallet Service skeleton, double-entry ledger schema
- [ ] Day 4 — Transfer endpoint with idempotency keys, MapStruct DTOs
- [ ] Day 5 — Kafka integration, Outbox pattern
- [ ] Day 6 — Notification Service consumer, multithreading basics
- [ ] Day 7 — Multithreaded reconciliation batch job
- [ ] Day 8 — API Gateway with Resilience4j (circuit breaker, retry)
- [ ] Day 9 — Redis caching layer
- [ ] Day 10 — Postgres index tuning, EXPLAIN ANALYZE evidence
- [ ] Day 11 — Testcontainers integration tests
- [ ] Day 12 — Multi-service Docker Compose, GitHub Actions CI
- [ ] Day 13 — Fraud rules engine + observability
- [ ] Day 14 — Architecture docs, system design walkthrough

## Author

**Ankur Salunke** — Backend engineer, ex-greytHR

[LinkedIn](https://linkedin.com/in/ankur-salunke-f98) · [GitHub](https://github.com/ankursalunke98)