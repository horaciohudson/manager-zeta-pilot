# ZetaPilotAI Manager — Development Roadmap

Version 1.0 — Phases reordered according to ADR-005, which moves extension integration earlier. Every phase has tasks and a definition of done.

> **Golden rule:** Do not start a phase until the previous phase meets its exit criteria. Commercial Phase 6 may start only after the go/no-go KPIs are met (pending decision P4).

---

## Phase 0 — Project foundation

- [ ] Approve the vision, architecture, and ADR documents.
- [ ] Resolve pending decisions P5 (domain/subdomains) and P8 (token and grace-period values).
- [ ] Create a dedicated Git repository for the Manager.
- [ ] Define go/no-go KPIs and targets (P4) and record them in [decisions.md](decisions.md).

**Exit criteria:** Documents approved, repository created, and P4/P5/P8 decided.

## Phase 1 — Core infrastructure

- [ ] Scaffold the backend (Spring Boot 3.3.3, Java 17, Maven wrapper) using the layered package structure and dependency versions described in `architecture.md` §3.
- [ ] Scaffold the frontend (React + TypeScript + Vite) with the standard `src` structure: auth, components, contexts, hooks, pages, routes, services, styles, types, and utils.
- [ ] Configure PostgreSQL and Flyway with an initial versioned migration.
- [ ] Create a development Docker Compose setup for the backend, frontend, and database.
- [ ] Configure Spring Security (JWT and rotating refresh tokens, roles, secure web-panel cookies, and no tokens in `localStorage`; ADR-012).
- [ ] Add standardized error handling (RFC 7807), Bean Validation, and base auditing.
- [ ] Integrate transactional email through panel-configurable SMTP, with encrypted-at-rest credentials and connection/email tests (ADR-011).
- [ ] Add health checks, structured logs, and environment-specific configuration.

**Exit criteria:** Compose starts all three containers, the health endpoint responds, and a development login/JWT flow works.

## Phase 2 — User accounts

- [ ] Registration with expiring email-verification tokens.
- [ ] Sign-in, sign-out, token refresh, password recovery, and password changes.
- [ ] `ROLE_USER` and `ROLE_ADMIN` roles; bootstrap the first administrator.
- [ ] Read and update personal information.
- [ ] Account deletion/anonymization under LGPD and consent records.
- [ ] Brute-force protection and rate limiting for authentication endpoints.
- [ ] Automatically provision a FREE license at registration.
- [ ] Publish a privacy policy and terms of use (legal draft).
- [ ] Unit and integration tests for authentication flows.

**Exit criteria:** A user can register, verify their email, sign in, recover a password, and delete their account through the API and frontend.

## Phase 3 — Devices and licenses

- [ ] `POST /api/devices/activate` with extension UUID and metadata; retain activation history.
- [ ] Allow users to view and remove device telemetry records, without device-count enforcement.
- [ ] Implement `GET /api/licenses/me` and `POST /api/licenses/validate` for active, suspended, expired, and canceled states.
- [ ] Implement `ProductVersion` with the minimum supported and latest versions.
- [ ] Add account, license, device, and version panels for users.
- [ ] Test all license states and transitions.

**Exit criteria:** The API validates licenses and registers devices, with every state covered by tests.

## Phase 4 — Vertical slice: extension integration ⭐ (critical path)

- [ ] Browser-based sign-in and `vscode://` callback with a one-time code (ADR-008).
- [ ] Generate and persist the device UUID in the extension (ADR-007).
- [ ] Activate devices and validate licenses at startup and every 24 hours.
- [ ] Implement the architecture's grace policy: seven-day cache; a network failure does not mean an invalid license.
- [ ] Add extension screens and flows for sign-in, license status, and update notices.
- [ ] Replace authentication mechanisms inherited from Cline.
- [ ] Integration-test the extension and Manager, including Manager downtime.

**Exit criteria:** A fresh extension installation can sign in, activate a device, validate its license, and operate offline for seven days. **This is the public Phase 1 launch milestone.**

## Phase 5 — Administration

- [ ] Admin dashboard to view, edit, and disable users.
- [ ] Manage licenses (suspend, reactivate, cancel) and devices.
- [ ] Manage product versions.
- [ ] Define and display registration, active-user, and device metrics (P6).
- [ ] Provide audit-log lookup.
- [ ] Deploy to the production VPS with subdomains, HTTPS, backups, and monitoring.

**Exit criteria:** An administrator can manage users, licenses, and devices without database access; production is online and a backup restore has been tested.

### Commercial launch gate

Before Phase 6, validate KPIs (P4) and decide P2 (payments and tax), P3 (prices and margins), and P7 (gateway providers).

## Phase 6 — Commercialization: AI gateway and credits

- [ ] Add the `gateway` module: streaming proxy, master keys in secrets, and no content logging.
- [ ] Add the `credit` module: `CreditWallet`, append-only `CreditTransaction` ledger, and versioned `ModelPricing`.
- [ ] Add ZetaPilotAI as a provider in the extension.
- [ ] Add per-user spending limits, rate limiting, global and per-user kill switches, and balance reservation.
- [ ] Integrate a payment gateway (P2) with idempotent webhooks.
- [ ] Add credit purchases, history, and balances to the user dashboard.
- [ ] Provide welcome credits to new users and the existing free-user base (ADR-006).
- [ ] Add usage dashboards with per-model and per-period spending and limit alerts.
- [ ] Publish gateway-specific terms explaining that prompts pass through the Manager without being stored (LGPD).
- [ ] Review availability and backups for the financial ledger; consider a lower RPO (revisit ADR-009).
- [ ] Load-test streaming and test ledger reconciliation.

**Exit criteria:** A user can buy credits, use the ZetaPilotAI streaming provider, and see accurate debits. The kill switch is tested, and ledger reconciliation reports no discrepancies.

## Phase 7 — Optional future work

- [ ] Monthly plans that include a credit allowance; never charge solely for BYOK extension access.
- [ ] Plans for teams and organizations.
- [ ] Advanced usage reports.

---

## Visual summary

```
Phase 0     Phase 1       Phase 2       Phase 3        Phase 4          Phase 5      [KPI GATE]   Phase 6
Docs     →  Foundation →  Accounts  →  Devices +   →  EXTENSION ⭐  →  Admin +   → AI Gateway +
                                      licenses       vertical slice     production    credits
```

Phase 4 is the central milestone: without it, the Manager is only a registration system, not a product. Extension integration was therefore moved earlier than in the original plan (ADR-005).