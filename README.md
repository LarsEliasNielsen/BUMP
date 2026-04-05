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
2. Fill in your database credentials and JWT secret in `application-local.yaml`:
   ```yaml
   spring:
     datasource:
       username: postgres
       password: your-password-here
   bump:
     security:
       jwt:
         secret: <base64url-encoded HMAC-SHA256 signing key>
   ```

`application-local.yaml` is gitignored and will never be committed. The datasource URL defaults to `jdbc:postgresql://localhost:5432/bump` and can also be overridden in the local file if needed.

**Production:** the application reads `BUMP_JWT_SECRET` from the environment at startup and will refuse to start if the decoded value is shorter than 32 bytes. The value must be a base64url-encoded, cryptographically random 256-bit key — generated locally with `openssl rand -base64 32 | tr '+/' '-_' | tr -d '='` or sourced from a secrets manager (AWS Secrets Manager, HashiCorp Vault, etc.).

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
| V3 | Create `tenants` table |
| V4 | Create `users` table |

## API Documentation

The application exposes an interactive OpenAPI 3 specification via Swagger UI. Start the application and open:

| Interface | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Raw OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

Both endpoints are publicly accessible (no authentication required).

## API Endpoints

Full request/response contracts are available in the Swagger UI. Short reference:

**Accounts**

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/accounts` | None | Register a new tenant account |

**Authentication**

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/auth/login` | None | Authenticate and receive a JWT token |

**Usage Events**

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/usage-events` | None (Story 3) | Submit a usage event (idempotent) |
