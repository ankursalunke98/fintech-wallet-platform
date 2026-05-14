# Fintech Wallet Platform

A backend system for digital wallet operations modeled on production fintech patterns. Implements an immutable double-entry ledger, idempotent money transfers, and optimistic locking for concurrent transaction safety.

Built as a portfolio piece for fintech and payments backend roles. The architecture follows patterns used by Stripe, Razorpay, and other regulated payment processors.

---

## Why this exists

Most "wallet" or "payments" tutorials treat balance as a column you increment and decrement. That approach fails catastrophically in three predictable ways:

1. **No audit trail.** A single mutable balance tells you the current state but loses the history of how that state was reached.
2. **No reversibility.** A buggy update is impossible to reverse cleanly without a separate audit log.
3. **Concurrency corruption.** Without locking, two simultaneous transfers can read the same balance and both decrement it, creating money out of thin air.

Real fintech systems handle this with a **double-entry ledger**: balance is never stored, only derived. Every transaction creates exactly two atomic entries — a debit from one account and a credit to another — and balance is `SUM(credits) − SUM(debits)`. This guarantees audit history by construction, makes reversals just another transaction, and combined with optimistic locking on accounts, eliminates the race-condition class of bugs entirely.

This project implements that model end-to-end, with the additional production patterns of idempotency keys (safe retries) and proper error semantics.

---

## What's implemented

### User registration
- Email-keyed registration with bcrypt password hashing
- Bean Validation for input correctness, with structured error responses
- `409 Conflict` on duplicate email registration

### Wallet creation
- One USER account per user, with optional initial deposit
- System accounts (`SYSTEM_DEPOSIT`, etc.) created lazily on first use, modeling external money flow
- Initial deposit creates a transaction with two ledger entries atomically

### Balance query
- Balance is computed on demand from immutable ledger entries
- `SUM(CASE WHEN entry_type = 'CREDIT' THEN amount ELSE -amount END)` aggregated per account

### Transfer with idempotency
- `Idempotency-Key` header required; SHA-256 hash of the request body cached against the key
- Duplicate requests with the same key + body return the cached response (no double-execution)
- Same key with a different body returns `422` (detects client-side bugs)
- Optimistic locking via JPA `@Version` on accounts prevents concurrent transfer races
- Insufficient balance returns `422` with available and requested amounts in the error body

### Error handling
- Centralized via `@RestControllerAdvice`, with a uniform `ErrorResponse` shape across all endpoints
- HTTP status codes follow REST semantics: `400` for malformed input, `404` for missing resources, `409` for state conflicts, `422` for business rule violations, `500` only for unexpected failures

---

## Architecture

```
HTTP Request
    │
    ▼
┌──────────────────────────┐
│      Controller layer    │   Handles HTTP, validation triggers,
│  (UserController, etc.)  │   status codes. No business logic.
└──────────────────────────┘
    │
    ▼
┌──────────────────────────┐
│       Service layer      │   Business logic, transactions,
│   (TransferService,      │   orchestration. @Transactional
│    WalletService, ...)   │   boundaries here.
└──────────────────────────┘
    │
    ▼
┌──────────────────────────┐
│     Repository layer     │   Spring Data JPA interfaces.
│  (Spring Data JPA)       │   Derived query methods + JpaRepository.
└──────────────────────────┘
    │
    ▼
┌──────────────────────────┐
│      JPA / Hibernate     │   Entity mapping, lifecycle callbacks,
│                          │   optimistic locking, transactions.
└──────────────────────────┘
    │
    ▼
┌──────────────────────────┐
│       PostgreSQL         │   Flyway-managed schema with
│                          │   CHECK constraints and indexes.
└──────────────────────────┘
```

The layering rule: each layer imports only from layers below it. Controllers never touch repositories directly; services never know they're being called over HTTP. This makes services unit-testable in isolation (no Spring context required) and allows the persistence layer to change without affecting the API contract.

### Database schema

Five domain tables:

| Table | Purpose |
|---|---|
| `users` | Identity records |
| `accounts` | Where money lives — user wallets and system accounts |
| `transactions` | High-level money events (DEPOSIT, WITHDRAWAL, TRANSFER) |
| `ledger_entries` | Individual debits and credits — the source of truth for all balances |
| `idempotency_keys` | Cached responses for transfer retries, with 24-hour TTL |

`accounts.version` (JPA `@Version`) enables optimistic locking. `ledger_entries` and `transactions` have no `updated_at` column — they're append-only by design.

### The double-entry invariant

Every transaction writes exactly two ledger entries with the same `transaction_id`, one DEBIT and one CREDIT, with the same amount and currency. The system-wide integrity invariant:

```sql
SELECT COALESCE(SUM(
    CASE WHEN entry_type = 'CREDIT' THEN amount ELSE -amount END
), 0) AS total
FROM ledger_entries;
```

This sum should always equal zero in a healthy system. It's the property auditors check, and it's what makes the ledger self-verifying.

---

## Stack

| Concern | Tool |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Security | BCrypt password hashing (Spring Security Crypto) |
| Build | Maven |
| Containerization | Docker / Docker Compose (Postgres) |
| JSON | Jackson |
| Boilerplate reduction | Lombok |

Monetary amounts use `BigDecimal` end-to-end — never `float` or `double`. Postgres `DECIMAL(19, 4)` maps cleanly to it.

---

## Running locally

### Prerequisites
- Java 21
- Docker
- Maven 3.9+

### Setup

```bash
git clone https://github.com/ankursalunke98/fintech-wallet-platform.git
cd fintech-wallet-platform
docker compose up -d
cd user-service
./mvnw spring-boot:run
```

Postgres comes up on `5432`. The Spring Boot service comes up on `8081`. Flyway applies all migrations on boot. The API is then reachable under `http://localhost:8081/api/v1/`.

### API

```
POST   /api/v1/users/register
POST   /api/v1/wallets
GET    /api/v1/wallets/{accountId}/balance
POST   /api/v1/transfers       (requires Idempotency-Key header)
```

Smoke test:

```bash
# Register a user
curl -X POST http://localhost:8081/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"secret123","fullName":"Alice"}'

# Create a wallet with 1000 initial deposit
curl -X POST http://localhost:8081/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"initialDeposit":1000}'

# Check balance
curl http://localhost:8081/api/v1/wallets/1/balance

# Transfer with idempotency key
curl -X POST http://localhost:8081/api/v1/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{"fromAccountId":1,"toAccountId":2,"amount":500}'
```

---

## Design notes

### Why idempotency keys are required, not optional

The transfer endpoint requires an `Idempotency-Key` header rather than treating it as optional. This is deliberate. Network failures cause clients to retry; without an idempotency key, the server has no way to distinguish a retry from a new request, leading to duplicate transfers. Making the header required forces clients into the correct integration pattern.

The key is hashed against the request body (SHA-256) so that reusing the same key with different parameters returns `422` rather than the previous cached response. This catches the class of client bugs where a key is generated once and accidentally reused across distinct operations.

### Why optimistic locking, not pessimistic

For wallets with low to moderate contention per account, optimistic locking via `@Version` performs better than pessimistic row locks. Two concurrent transfers attempting to modify the same account both proceed; only one succeeds at commit. The other receives `OptimisticLockException` and can retry. This avoids holding database locks across the transaction body, which is particularly important when transfers involve external service calls.

Pessimistic locking (`SELECT ... FOR UPDATE`) becomes appropriate for high-contention scenarios — a single shared account hit by thousands of concurrent operations. Wallets don't fit this profile.

### Why balance is derived, not stored

Storing a `balance` column would be faster to read but creates two failure modes: (1) the column can drift out of sync with the underlying entries if any write path is missed, and (2) it requires careful update ordering and locking around every transaction. Derived balance has neither problem — the ledger is the single source of truth, and balance is always consistent with it by definition.

For very high-volume systems, a common optimization is to store a periodically-checkpointed balance (every N entries or every hour) and compute the current balance as `checkpoint + recent entries`. This isn't necessary at smaller scale and adds complexity.

### Why two-pass HashMap, not one-pass

(For the related DSA work on character frequency analysis.) Counting frequencies and then finding the first non-repeating character cannot be done in a single pass — when you encounter `'l'` at index 0 of `"loveleetcode"`, you don't yet know whether it will appear again. The two-pass structure is necessary, and it doesn't change the asymptotic complexity: both passes are O(N), total still O(N).

---

## Currently in development

These patterns are next, building toward production-grade completeness:

- **Outbox pattern with Kafka** for cross-service event propagation (notification service, fraud rules, downstream consumers)
- **Redis cache-aside** for hot-path balance reads, with invalidation on transfer
- **Resilience4j circuit breakers** around external bank API calls, with fallback handling
- **JWT authentication** with Spring Security filter chain on protected endpoints
- **Mockito + Testcontainers** test suite covering both unit and integration paths
- **Multi-service Docker Compose** orchestration with notification-service and Kafka

---

## Repository layout

```
fintech-wallet-platform/
├── user-service/                     # Spring Boot module (currently the only service)
│   ├── src/main/java/com/ankur/userservice/
│   │   ├── controller/               # HTTP layer
│   │   ├── service/                  # Business logic
│   │   ├── repository/               # Spring Data JPA
│   │   ├── entity/                   # JPA entities + enums
│   │   ├── dto/                      # Request/response shapes
│   │   ├── exception/                # Custom exceptions + global handler
│   │   └── config/                   # Beans (BCrypt encoder)
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/             # Flyway migrations (V1, V2, V3)
│   └── pom.xml
├── docker-compose.yml                # Postgres
└── README.md
```

---

## Notes on what's not here yet

The project is deliberately scoped. Things explicitly out of scope for now:

- Multi-currency conversion (currency is INR-only throughout)
- Cross-border settlement
- Fee calculation engine
- KYC / regulatory reporting
- Real banking integration (NEFT/RTGS/IMPS adapters)
- Production monitoring (Prometheus, Grafana, distributed tracing)

Some of these are listed for genuine technical reasons — currency conversion, for instance, requires careful handling of FX rates, rounding rules, and counter-party currency accounts that significantly expand the domain model. Others are roadmap items that I'll add as the project matures.

The goal here is a clean implementation of the core patterns, not a complete payment processor.