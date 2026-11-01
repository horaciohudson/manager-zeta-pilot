# ZetaPilotAI Manager — Technical Architecture

Version 1.0. Decisions are recorded in [decisions.md](decisions.md).

---

## 1. Architecture overview

```
                         INTERNET
                             |
                       NGINX / HTTPS
                       |           |
                corporate site   Manager subdomains
                (WordPress)       |           |
                               app.*        api.*
                               (React)   (Spring Boot)
                                             |
                                 +-----------+------------+
                                 |                        |
                           Management REST API       AI Gateway (Phase 2)
                           (auth, users,             (metered AI-provider
                            licenses, devices)         proxy)
                                 |                        |
                                 +-----------+------------+
                                             |
                                        PostgreSQL
                                     (dedicated database)

       VS CODE EXTENSION (ZetaPilotAI)
          |                |                  |
    Management API    AI Gateway (Phase 2)   Direct providers (BYOK)
```

## 2. Technology stack (see ADR-003)

Follow the established geo-limites conventions and dependency versions to simplify cross-project maintenance.

- **Backend:** Java 17, Spring Boot 3.3.3, Maven wrapper, Spring Web and WebFlux (gateway streaming), Spring Data JPA, Spring Security, Bean Validation, Flyway (`flyway-core` and `flyway-database-postgresql`), PostgreSQL, Lombok, JJWT 0.12.3, springdoc-openapi 2.5.0, Spring Boot Mail, dotenv-java, and H2 for tests.
- **Frontend:** React, TypeScript, Vite, plain CSS (area-specific stylesheets), React Router, contexts, and hooks.
- **Database:** Dedicated PostgreSQL database. Flyway migrations live in `Backend/src/main/resources/db/migration` and use the `V{n}__description.sql` naming convention.
- **Infrastructure:** Separate Dockerfiles for the backend and frontend, Nginx serving the frontend build, root-level Docker Compose, environment-specific configuration, required HTTPS, and independent logs and backups.
- **Transactional email:** Administrator-configurable SMTP settings stored in the database, adapting geo-limites `SmtpSettings` and `SmtpMailService` (ADR-011). Encrypt the password at rest using a secret supplied through the environment; never return it from the API.

### 2.1. Reusable design references from geo-limites

Use geo-limites as a design reference, not as a source for code coupled to its memorial domain.

| Pattern | geo-limites reference |
|---|---|
| Credits, packages, transaction ledger, and pricing | `CreditService`, `CreditController`, `UserCredits`, `CreditPurchase`, `CreditTransaction`, migrations V11/V22 |
| Configurable transactional SMTP | `SmtpSettings`, `SmtpMailService`, `AdminSettingsController` |
| Persisted, revocable refresh tokens | `model/RefreshToken`, `AuthService` |
| Auditing | `AuditBase`, `AuditLog`, `AuditLogService` |
| Email verification | Migration V4 and `VerifyEmail`/`ResendVerification` pages |
| Layered package structure | `config/ controller/ dto/ enums/ exception/ mapper/ model/ security/ service/ util/` |

This stack matches the team's existing experience and VPS environment. Managed alternatives such as Supabase and Firebase were rejected because Phase 2 requires control of AI gateway streaming and the financial ledger, as well as hosting data on our own infrastructure.

## 3. Backend package structure

Use the geo-limites layered package convention, without carrying over its memorial domain:

```
Backend/src/main/java/com/zetapilot/manager/
├── ManagerApplication.java
├── DataInitializer.java
├── config/        # SecurityConfig, WebConfig, OpenApiConfig, AsyncConfig
├── controller/    # REST controllers
├── dto/           # Request and response models
├── enums/         # Roles, license status, and other enums
├── exception/     # Domain exceptions and global handlers
├── mapper/        # Entity/DTO mapping
├── model/         # JPA entities and audit base
├── repository/    # Spring Data repositories
├── security/      # JWT filters, token services, and helpers
├── service/       # Application and domain services
└── util/          # Stateless utilities
```

## 4. Identity and authentication

### 4.1. User and roles

- A user is identified by a unique, normalized email address.
- Initial roles are `ROLE_USER` and `ROLE_ADMIN`. Additional roles require a documented product need.
- Administrator access is assigned only by the Manager; never accept roles from public registration requests.
- Passwords use BCrypt. Passwords and raw verification/reset tokens are never stored in plaintext.
- Require email verification before granting a license or enabling extension sign-in.

### 4.2. JWT access and refresh tokens

| Token | Lifetime | Storage | Purpose |
|---|---:|---|---|
| Access JWT | 15 minutes | Memory only | API authorization |
| Refresh token | 7 days, rotating | HttpOnly, Secure, SameSite cookie on web; VS Code SecretStorage in extension | Issue a new access token |
| One-time code | 60 seconds, single use | Hashed in the database | Exchange browser authentication for extension tokens |

- Sign access tokens with HS256 using a secret of at least 256 bits, provided outside the repository.
- Include `sub` (user ID), `roles`, `iat`, and `exp`; never include personal or secret data.
- Generate refresh tokens with a cryptographically secure random generator. Persist only a hash; rotate on use and revoke on sign-out, password change, or security events.
- Detect refresh-token reuse and revoke the affected token family.
- Apply rate limits to sign-in, registration, verification, and recovery endpoints.
- Return authentication failures with consistent RFC 7807 problem details.

### 4.3. Browser and extension sign-in

- **Web dashboard:** use `HttpOnly`, `Secure` cookies with `SameSite=Lax` or stricter; add CSRF protection for state-changing requests. Never persist tokens in `localStorage` or `sessionStorage`.
- **VS Code extension:** open the system browser for sign-in, then redirect to a registered `vscode://` URI containing a short-lived one-time code. Exchange it for tokens and store them only in VS Code SecretStorage. The extension must never collect the user's password.
- Register and allowlist the exact extension callback URI. Validate state and consume each code atomically once.

## 5. License validation and offline behavior

The license response includes `status`, `plan`, `validatedAt`, `expiresAt` (when applicable), `minimumSupportedVersion`, and `latestVersion`.

| Condition | Extension behavior |
|---|---|
| Revalidation | At startup and every 24 hours |
| Last successful validation | Cache for up to 7 days |
| Manager unavailable within grace period | Continue normally and show a discreet notice |
| Grace period expired | Require revalidation to continue |
| Explicit suspended/canceled response | Block immediately; no grace period |

**A communication failure must never be interpreted as an invalid license.** Only an explicit server response changes the license state.

## 6. AI gateway (Phase 2; architectural seams in Phase 1)

### 6.1. Request flow

```
Extension --(JWT)--> POST /api/ai/v1/chat
                         |
                    1. authenticate user
                    2. check credit balance (fail fast if insufficient)
                    3. select provider master key from secrets
                    4. forward request to provider (SSE streaming)
                    5. read input/output token usage
                    6. calculate cost using current ModelPricing
                    7. debit CreditTransaction ledger
                    8. return stream to client
```

### 6.2. Requirements

- Store provider master keys only in container secrets or environment variables.
- Never log prompt or response contents. Log metadata only: user, model, token counts, cost, timestamp, duration, and status.
- Meter from the provider response `usage` field (enable `include_usage` for streaming). Use approximate counts only when a provider does not report usage.
- From launch, enforce per-user and per-period spending limits, rate limits, global and per-user kill switches, and estimated balance reservation at request start.
- Use an append-only `CreditTransaction` ledger with `PURCHASE`, `BONUS`, `DEBIT`, `REFUND`, and `ADJUSTMENT` entries. Reconcile derived or materialized balances.
- Version `ModelPricing` by effective date and include a margin over provider cost.
- Keep streaming pass-through without buffering the full response; set appropriate timeouts.

## 7. Data model

| Entity | Responsibility | Phase |
|---|---|---:|
| User | Account registration | 1 |
| UserRole | Roles and permissions | 1 |
| Product | Managed products | 1 |
| LicensePlan | Plans (FREE; CREDITS in the future) | 1 |
| License | User license status and plan | 1 |
| Device | Extension-generated device UUID | 1 |
| DeviceActivation | Activation and validation history | 1 |
| RefreshToken | Hashed, revocable authentication renewal | 1 |
| ProductVersion | Available versions and policy (see §7.1) | 1 |
| EmailVerification / PasswordReset | Verification and recovery tokens | 1 |
| AuditLog | Operation audit history | 1 |
| CreditWallet | User credit balance | 2 |
| CreditTransaction | Append-only credit ledger | 2 |
| ModelPricing | Effective-dated model pricing and margin | 2 |
| GatewayUsage | Usage metadata without prompt content | 2 |
| Payment / PaymentWebhook | Payment gateway integration (later Phase 2) | 2 |

Conventions: table names use the `tab_` prefix; primary keys are UUIDs for main entities; audit fields use `created_at`, `updated_at`, and `created_by`; add indexes for lookup keys such as `user_id`, device UUID, and license status.

### 7.1. Product version policy

The VS Code Marketplace distributes extension updates. `ProductVersion` is used to communicate the minimum API-supported version, notify users of available updates, and block versions with critical security issues. Do not duplicate Marketplace update functionality.

## 8. Initial API contract

```
# Authentication
POST   /api/auth/register
POST   /api/auth/login                 # web frontend
POST   /api/auth/extension/code        # issue one-time code (authenticated web session)
POST   /api/auth/token                 # exchange code for extension tokens
POST   /api/auth/refresh
POST   /api/auth/logout
POST   /api/auth/forgot-password
POST   /api/auth/reset-password
GET    /api/auth/confirm-email

# User
GET    /api/users/me
PATCH  /api/users/me
POST   /api/users/me/change-password
DELETE /api/users/me                   # deletion/anonymization (LGPD)

# Devices
POST   /api/devices/activate
GET    /api/devices
GET    /api/devices/{id}
DELETE /api/devices/{id}               # remove telemetry record

# Licenses
GET    /api/licenses/me
POST   /api/licenses/validate          # extension: device UUID and version

# Versions
GET    /api/versions/latest
GET    /api/versions/minimum-supported

# Administration (ROLE_ADMIN)
GET    /api/admin/users | /users/{id} | PATCH /users/{id}/status
GET    /api/admin/devices | DELETE /devices/{id}
GET    /api/admin/licenses | PATCH /licenses/{id}
GET    /api/admin/metrics
GET    /api/admin/audit

# Phase 2 — AI gateway and credits
POST   /api/ai/v1/chat                 # metered streaming proxy
GET    /api/credits/balance
GET    /api/credits/transactions
GET    /api/credits/pricing
POST   /api/credits/purchase            # payment integration TBD
```

Use RFC 7807 problem details for errors. Version REST paths under `/api`; the gateway uses `/api/ai/v1/`.

## 9. Non-functional requirements

- **Availability:** A single VPS is an accepted single point of failure in Phase 1 (ADR-009). The seven-day grace period hides short outages. Target 99% monthly availability in Phase 1; revisit for Phase 2, when the gateway is on the critical path for AI requests.
- **Backups:** Daily PostgreSQL dump, 30-day retention, and quarterly restore tests. Initial RPO is 24 hours. Review for a lower RPO and continuous WAL archiving when the Phase 2 financial ledger launches.
- **Observability:** Structured JSON logs per container with rotation, plus monitored `/actuator/health` checks.
- **Performance:** Sized connection pool and streaming pass-through without full-response buffering.

## 10. LGPD and legal requirements

- Publish a privacy policy and terms of service before registration opens.
- Record consent versions and timestamps during registration.
- Support `DELETE /api/users/me` with personal-data anonymization, retaining only records required for legal or tax obligations.
- Keep usage telemetry minimal and documented, and provide opt-out where applicable.
- In Phase 2, disclose that prompts pass through the Manager gateway and are not stored.
- Preserve Cline Apache 2.0 notices; evaluate ZetaPilotAI trademark registration.

## 11. Deployment

- Deploy independent containers on the existing VPS: `manager-backend`, `manager-frontend` (static Nginx), and `manager-db` (PostgreSQL with its own volume).
- Configure Nginx subdomains `app.<domain>` (frontend) and `api.<domain>` (backend) with dedicated certificates and no path sharing with WordPress.
- Maintain independent backups and logs. Do not change existing systems' containers or databases without authorization.
- Keep the root-level `docker-compose.yml` consistent with geo-limites conventions.

## 12. Repository structure

```
manager-zeta-pilot/
├── Project/                 # project documentation
├── Backend/
│   ├── src/main/java/com/zetapilot/manager/...
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-dev.properties
│   │   └── db/migration/    # Flyway: V1__baseline.sql, V2__...
│   ├── Dockerfile
│   └── pom.xml              # includes Maven wrapper
├── Frontend/
│   ├── src/
│   ├── nginx/default.conf
│   ├── Dockerfile
│   ├── index.html
│   └── package.json
├── docker-compose.yml
├── .env.docker.example
└── README.md
```

The Manager has its own Git repository, separate from the ZetaPilotAI extension repository.