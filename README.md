# BUMP — Billing and Usage Metering Platform

A backend service for tracking and metering usage events across users, services, and products.

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| [Kotlin](https://kotlinlang.org/) | 2.2.21 | Primary language |
| [Spring Boot](https://spring.io/projects/spring-boot) | 4.0.5 | Application framework |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | — | Data access layer |
| [Spring Security](https://spring.io/projects/spring-security) | — | Authentication and authorization |
| [PostgreSQL](https://www.postgresql.org/) | 18+ | Relational database |
| [Flyway](https://flywaydb.org/) | 11+ | Database migrations |
| [Java](https://openjdk.org/) | 24 | JVM runtime |
| [springdoc-openapi](https://springdoc.org/) | 2.8+ | OpenAPI 3 spec + Swagger UI |

## Prerequisites

- JDK 24
- PostgreSQL running on `localhost:5432` with a database named `bump`

## Configuration

Credentials are kept out of version control using a local override file:

1. Copy the example file:
   ```bash
   cp src/main/resources/application-local.yaml.example src/main/resources/application-local.yaml
   ```
2. Fill in your database credentials in `application-local.yaml`:
   ```yaml
   spring:
     datasource:
       username: postgres
       password: your-password-here
   ```

`application-local.yaml` is gitignored and will never be committed. The datasource URL defaults to `jdbc:postgresql://localhost:5432/bump` and can also be overridden in the local file if needed.

## Gradle Commands

### Run the application

```bash
./gradlew bootRun
```

The application starts on port `8080`.

### Build

```bash
./gradlew build
```

### Run tests

```bash
./gradlew test
```

### Compile only

```bash
./gradlew compileKotlin
```

### Clean build outputs

```bash
./gradlew clean
```

### Build an executable JAR

```bash
./gradlew bootJar
```

The JAR is output to `build/libs/`.

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/` and run automatically on startup.

| Version | Description |
|---|---|
| V1 | Create `usage_events` table |
| V2 | Add `idempotency_key` column to `usage_events` |

## API Documentation

The application exposes an interactive OpenAPI 3 specification via Swagger UI. Start the application and open:

| Interface | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Raw OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

Both endpoints are publicly accessible (no authentication required).

## API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/` | Welcome message |
| `POST` | `/usage-events` | Submit a usage event |

### POST /usage-events

Submits a usage event. Requests are idempotent — submitting the same `idempotencyKey` twice returns `409 Conflict` without creating a duplicate.

**Request body:**

```json
{
  "customerId": "customer-123",
  "service": "compute",
  "product": "vm-standard",
  "eventDateTime": "2026-03-28T10:00:00Z",
  "idempotencyKey": "a4f9b81c-1234-4c8f-9abc-2f3d5e6a7b8c"
}
```

**Response** `201 Created`:

```json
{
  "id": "e29b6e3a-1234-4c8f-9abc-2f3d5e6a7b8c",
  "customerId": "customer-123",
  "service": "compute",
  "product": "vm-standard",
  "eventDateTime": "2026-03-28T10:00:00Z",
  "idempotencyKey": "a4f9b81c-1234-4c8f-9abc-2f3d5e6a7b8c"
}
```

**Response** `409 Conflict` — returned when the `idempotencyKey` has already been used.
