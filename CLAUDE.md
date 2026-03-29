# BUMP — Billing and Usage Metering Platform

## What We Are Building

BUMP is a **usage-based billing platform** similar to what SaaS companies use to charge customers based on API usage, storage, or compute. The system ingests high-volume usage events, aggregates them into billing periods, applies complex pricing rules (tiered, volume pricing), and generates invoices asynchronously.

### Business Problem

Many SaaS companies struggle with accurately tracking usage and turning it into correct, auditable invoices—especially when usage is high-volume, pricing is complex, and billing must be reliable.

Key constraints:
- **High write throughput** — usage events at scale
- **Strong correctness** — billing deals with money, no mistakes allowed
- **Delayed computation** — billing cycles
- **Complex pricing models** — tiers, discounts, volume
- **Auditability & traceability** — every invoice must be fully explainable

### Core System Components

| Component | Responsibility |
|---|---|
| Usage Ingestion Service | Accepts, validates, and stores raw usage events |
| Usage Aggregation Engine | Processes events into billable units per customer + billing period |
| Pricing Engine | Applies pricing rules (tiers, limits, discounts) |
| Billing Engine | Generates invoices at end of billing cycle |
| Payment & Lifecycle Manager | Tracks payment status, handles retries/failures |
| Notification System | Sends alerts (invoice ready, failed payment) |

### Event-Driven Flow

```
External system → UsageRecorded(customerId, metric, quantity)
                → Aggregation updates totals → UsageAggregated
                → BillingPeriodClosed (at cycle end)
                → Pricing calculates cost → InvoiceGenerated
                → PaymentAttempted → PaymentSucceeded | PaymentFailed
```

### Key Engineering Challenges

1. **Idempotency & Duplicate Events** — events may arrive multiple times; use idempotency keys and immutable append-only event store
2. **Eventual Consistency vs Accuracy** — real-time dashboards are approximate; billing view is reconciled from raw events before invoicing
3. **High Throughput Ingestion** — decouple ingestion from processing; buffer/queue, batch, partition by customer ID
4. **Auditability** — immutable raw event storage; full trace from invoice → aggregated usage → raw events
5. **Backfilling & Corrections** — support reprocessing from raw events; versioned invoices (never mutate, create revised)

---

## Project Roadmap (Epics)

### Phase 1 — Proof of Concept (Core SaaS Backbone)

| # | Epic | Goal |
|---|---|---|
| 1 | Authentication & Multi-Tenancy | Secure, tenant-isolated access with JWT |
| 2 | Customer & Subscription Management | Define who gets billed |
| 3 | Usage Ingestion API (Idempotent Core) | `POST /usage-events` with idempotency keys, immutable storage |
| 4 | Usage Aggregation Engine (Basic) | Aggregate raw events into billable units per customer/period |
| 5 | Basic Pricing Engine (Extensible Design) | Flat-rate/per-unit pricing with strategy pattern for extensibility |
| 6 | Invoice Generation (PoC Version) | Trigger billing period close, generate invoice with line items |
| 7 | Event-Driven Backbone (Lightweight) | Internal domain events: `UsageRecorded`, `UsageAggregated`, `InvoiceGenerated` |
| 8 | Minimal Observability & Admin APIs | View usage, view invoices, trace event to invoice |

### Phase 2 — Expansion (Portfolio-Grade SaaS)

| # | Epic | Goal |
|---|---|---|
| 9 | Advanced Pricing Engine | Tiered pricing, volume pricing, discounts |
| 10 | Automated Billing Cycles | Scheduled monthly billing, lifecycle states |
| 11 | Async Processing & Queue Integration | Queue abstraction (prep for AWS SQS), async ingestion pipeline |
| 12 | Auditability & Traceability | Full trace invoice → aggregated usage → raw events |
| 13 | Backfilling & Reprocessing | Re-run aggregation, versioned corrected invoices |
| 14 | Real-Time Usage Dashboard API | Fast eventually-consistent read model |
| 15 | Idempotency & Deduplication Hardening | Deduplication strategies across full pipeline |

### Phase 3 — Advanced / Interview Differentiation

| # | Epic | Goal |
|---|---|---|
| 16 | Multi-Currency & Tax (EU-ready) | VAT handling, currency conversion |
| 17 | Payment Integration | Payment lifecycle, retry logic, Stripe-style provider |
| 18 | Usage Anomaly Detection | Spike detection, alerting |
| 19 | Scalability & Partitioning | Partitioning by tenant/customer, horizontal scaling |

---

## Pricing Model Examples

**Tiered Pricing:**
- First 1,000 requests → free
- Next 9,000 → €0.01 each
- Above 10,000 → €0.005 each

**Volume Pricing:** All usage priced based on the final tier reached.

**Add-ons:** Discounts, coupons, custom enterprise pricing.

---

## Coding Guidelines

> *Explicit is better than implicit. Simple is better than complex. Complex is better than complicated. Readability counts. In the face of ambiguity, refuse the temptation to guess. There should be one obvious way to do it.*
> — Inspired by The Zen of Python

These guidelines apply to all code written in this project. They are not suggestions—treat them as invariants.

---

### 1. Architecture

This project applies **Hexagonal Architecture** (Alistair Cockburn, 2005), also known as **Ports & Adapters**, as codified in **Clean Architecture** (Robert C. Martin, 2017).

The central idea is the **Dependency Rule**: source code dependencies must always point *inward*. The domain sits at the center and knows nothing about the outside world. Infrastructure, frameworks, and delivery mechanisms (HTTP, queues, databases) are details — they live on the outside and depend on the domain, never the reverse.

```
                        ┌─────────────────────────────┐
                        │         Infrastructure       │
                        │  ┌───────────────────────┐  │
                        │  │      Application       │  │
                        │  │  ┌─────────────────┐  │  │
                        │  │  │     Domain      │  │  │
                        │  │  │  (no deps)      │  │  │
                        │  │  └─────────────────┘  │  │
                        │  └───────────────────────┘  │
                        └─────────────────────────────┘
          HTTP / Queue ──►  Controller / Consumer  ──►  Use Case  ──►  Domain
          Database      ◄──  JPA Adapter          ◄──  Port (interface)
```

#### Ports
Ports are **interfaces defined by the domain or application layer** that describe what the outside world must provide. They express a need without caring how it is fulfilled.

- `UsageEventRepository` — port for persistence. The domain defines it; infrastructure implements it.
- `DomainEventPublisher` — port for event publishing. The application layer defines it; infrastructure wires it to Spring or SQS.

#### Adapters
Adapters are **infrastructure implementations of ports**. They translate between the domain model and external concerns (SQL, JSON, HTTP, message queues).

- `UsageEventRepositoryAdapter` — implements `UsageEventRepository` using JPA.
- `UsageEventController` — translates HTTP requests into application commands and back.

#### Package Structure as the Hexagon

```
cloud.larn.bump
├── domain/             # Center of the hexagon. Zero framework dependencies.
│   ├── model/          # Aggregates, Entities, Value Objects
│   ├── event/          # Domain events (facts that happened)
│   └── repository/     # Port interfaces (what persistence must provide)
├── application/        # Orchestration layer. Depends on domain only.
│   ├── usecase/        # One class per use case (Clean Architecture Interactors)
│   └── port/           # Output port interfaces (e.g. DomainEventPublisher)
├── infrastructure/     # Adapters: implement ports, integrate frameworks.
│   ├── persistence/    # JPA entities, Spring Data repos, repository adapters
│   └── messaging/      # Event publisher adapter (Spring events → SQS/Kafka)
└── api/                # HTTP adapter: controllers, request/response models
```

#### Why This Matters for BUMP
- The domain can be **unit tested without Spring, JPA, or a database**.
- The persistence layer can be **swapped** (e.g., PostgreSQL → DynamoDB) by writing a new adapter without touching a single domain or application class.
- The event backbone can be **evolved** from Spring internal events (Phase 1) to AWS SQS (Phase 2) by replacing only the `DomainEventPublisher` adapter.

---

### 2. Philosophy

- **Explicit over implicit.** If something is not obvious from the types and names, it should be made explicit—through naming, type signatures, or a short comment explaining *why*, not *what*.
- **Simple over clever.** A straightforward solution that is easy to read is always preferable to a clever one that is hard to reason about.
- **Correctness over performance.** This is a billing system. Money must be correct. Optimize only when you have a measured problem.
- **Design for change.** The cost of a wrong abstraction is higher than the cost of a little duplication. Don't abstract until the pattern is clear.
- **Errors should never pass silently.** Invalid state must not be representable. Fail fast, loudly, and with a meaningful message.

---

### 3. Domain-Driven Design (DDD)

The domain is the heart of this system. Every technical decision must serve the domain model.

#### Ubiquitous Language
- Use domain terms consistently in code, tests, API contracts, and documentation. `UsageEvent`, `BillingPeriod`, `Invoice`, `Tenant`, `PricingTier` are domain concepts—not implementation details.
- Never leak infrastructure names into the domain. A `UsageEvent` is not a `UsageEventEntity` or `UsageEventDto` in the domain layer.

#### Aggregates and Value Objects
- Model money as a **Value Object**, never a primitive. Use `BigDecimal` with explicit currency.
- Idempotency keys, customer IDs, and tenant IDs are **Value Objects**, not raw `String` or `UUID`.
- Aggregates enforce their own invariants. If a `BillingPeriod` cannot be closed twice, that rule lives in the aggregate—not in a service.
- Prefer small aggregates. An aggregate is a consistency boundary, not a data container.

```kotlin
// Prefer this:
@JvmInline value class CustomerId(val value: UUID)
@JvmInline value class IdempotencyKey(val value: String)

data class Money(val amount: BigDecimal, val currency: Currency) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Currency mismatch" }
        return copy(amount = amount + other.amount)
    }
}
```

---

### 4. Event-Driven Design

Events are facts. They record something that *has happened*, expressed in past tense.

#### Domain Events
- Name events in past tense: `UsageRecorded`, `InvoiceGenerated`, `BillingPeriodClosed`, `PaymentFailed`.
- Events are **immutable data**. Use `data class` with `val` properties only.
- Every event carries: the aggregate ID it originated from, a timestamp, and the relevant state change.
- Consumers must be **idempotent**. Processing the same event twice must produce the same result.

```kotlin
data class UsageRecorded(
    val eventId: UUID = UUID.randomUUID(),
    val customerId: CustomerId,
    val metric: UsageMetric,
    val quantity: Long,
    val idempotencyKey: IdempotencyKey,
    val occurredAt: Instant
)
```

#### Event Publishing
- The **application layer** publishes events after a successful domain operation—not the domain itself.
- Use an abstraction (`DomainEventPublisher`) so the domain and application layers are not coupled to Spring's `ApplicationEventPublisher` or any queue technology.
- For Phase 1, internal Spring events are acceptable. Design the publisher interface so it can be swapped for a queue (SQS, Kafka) without changing callers.

#### Event Consumers
- A consumer handles one event type and does one thing. No fan-out logic inside a single handler.
- Consumers belong to the **application layer**. Infrastructure concerns (deserialization, acknowledgement) belong to the infrastructure adapter.

---

### 5. Extensibility & Design Patterns

Good architecture makes the right things easy and the wrong things hard.

#### Composition Over Inheritance
- Prefer composing behavior through interfaces and delegation over extending base classes.
- Use Kotlin's `by` delegation operator where it removes boilerplate without hiding intent.
- Abstract classes are acceptable for sharing infrastructure concerns (e.g., a base repository). They are not acceptable for sharing domain behavior.

#### Strategy Pattern — Pricing Engine
The pricing engine is the canonical use case for Strategy in this project. Adding a new pricing model must never require modifying existing code (Open/Closed Principle).

```kotlin
interface PricingStrategy {
    fun calculate(usage: AggregatedUsage, config: PricingConfig): Money
}

class TieredPricingStrategy : PricingStrategy { ... }
class VolumePricingStrategy : PricingStrategy { ... }
class FlatRatePricingStrategy : PricingStrategy { ... }
```

#### Factory Pattern — Aggregate Construction
Use factory methods or companion object factories for aggregates with complex construction logic. This keeps constructors simple and invalid states impossible.

```kotlin
data class Invoice private constructor(...) {
    companion object {
        fun generate(period: BillingPeriod, usage: AggregatedUsage, pricing: Money): Invoice { ... }
    }
}
```

#### Repository Pattern — Persistence Abstraction
Every aggregate has a repository interface in the domain layer. The domain calls `save(aggregate)` and `findById(id)`—never SQL, never JPA directly.

#### Builder / DSL Pattern — Test Data
Use builders or Kotlin DSLs for constructing test fixtures. Never construct domain objects inline across many tests—changes to constructors will cascade.

```kotlin
fun aUsageEvent(
    customerId: CustomerId = CustomerId(UUID.randomUUID()),
    metric: UsageMetric = UsageMetric.API_CALL,
    quantity: Long = 1L
) = UsageEvent(customerId = customerId, metric = metric, quantity = quantity, ...)
```

#### Observer / Event Pattern — Decoupled Reactions
Side effects (sending notifications, updating read models) must not live inside the primary business transaction. Publish a domain event; let a separate handler react.

---

### 6. Kotlin Style

Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). Highlights specific to this project:

#### Immutability First
- Prefer `val` over `var` everywhere. `var` requires justification.
- Prefer immutable collections (`List`, `Map`, `Set`) over mutable ones. Return mutable collections only when callers must mutate them—which should be rare.
- Domain objects are immutable. Mutations produce new instances.

#### Null Safety
- Never use `!!`. It is a runtime assertion that the compiler already asked you to handle properly.
- Represent optional domain concepts with explicit types (`sealed class`, nullable with intent) rather than `Optional<T>`.
- A `null` from a repository means "not found." Return a `Result<T>` or a domain-specific sealed type when the absence has business meaning.

#### Naming
- Packages: lowercase, no underscores — `com.bump.domain.model`
- Classes and interfaces: `UpperCamelCase` — `UsageEvent`, `PricingStrategy`
- Functions and properties: `lowerCamelCase` — `calculateCost()`, `billingPeriod`
- Constants: `SCREAMING_SNAKE_CASE` — `MAX_RETRY_ATTEMPTS`
- Domain events: noun + past participle — `InvoiceGenerated`, `UsageRecorded`
- Repository methods: `findBy…`, `save`, `delete` — consistent with DDD vocabulary

#### Functions
- Prefer expression bodies for simple, single-expression functions.
- Use named arguments when a call site has multiple parameters of the same type or boolean flags.
- Keep functions short. If a function needs a comment to explain its sections, it should be multiple functions.

#### Data Classes
- Use `data class` for Value Objects and domain events.
- Use regular classes for aggregates—equality by identity, not structural equality.

#### Sealed Classes / Result Types
- Model outcomes explicitly. A function that can fail in domain-meaningful ways should return a sealed type, not throw an exception.

```kotlin
sealed class UsageRecordResult {
    data class Recorded(val event: UsageEvent) : UsageRecordResult()
    data object Duplicate : UsageRecordResult()
    data class Invalid(val reason: String) : UsageRecordResult()
}
```

---

### 7. Testing

- **Test behavior, not implementation.** Tests verify that the system does the right thing, not how it does it.
- **Unit test the domain.** Domain logic has no dependencies—tests need no framework, no mocks, no Spring context. They should be fast and numerous.
- **Integration test the application layer.** Verify that use cases correctly coordinate domain + repository + event publishing.
- **Do not mock the database in persistence tests.** Use an in-memory or test-container database. Mock/prod divergence has caused billing bugs before.
- **Use the builder DSL pattern** for test fixtures. Keep test data construction out of test assertions.
- **Name tests as sentences.** `should reject duplicate usage event when idempotency key already exists`.

---

### 8. Architecture Invariants (Hard Rules)

These rules must not be violated without explicit discussion:

1. **The domain layer has no framework dependencies.** No Spring, no JPA, no Jackson annotations inside `domain/`.
2. **Never use primitives for domain identifiers.** Wrap every ID and key in a value class.
3. **Never represent money as `Double` or `Float`.** Always `BigDecimal` with a `Currency`.
4. **Aggregates enforce their own invariants.** No service is allowed to put an aggregate into an invalid state that the aggregate itself would reject.
5. **Events are immutable and past tense.** No mutable event fields. No imperative event names (`ProcessUsage` is a command, not an event).
6. **One use case per class.** Application services are thin orchestrators with a single public method.
7. **No business logic in controllers.** Controllers translate HTTP to application commands and back. Nothing more.
8. **Keep `README.md` up to date.** Any change that affects API contracts, migration history, prerequisites, or configuration must be reflected in `README.md` in the same session it is made.
9. **Run tests after changing tested code.** Any change to a test class or to a component covered by tests must be followed by running `./gradlew test` in the same session. Tests must pass before the session ends.
