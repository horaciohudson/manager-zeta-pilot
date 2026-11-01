# ZetaPilotAI Manager — Product Vision

An independent management platform for users, licenses, and AI credits for the ZetaPilotAI Visual Studio Code extension.

- **Version:** 2.0
- **Product:** ZetaPilotAI
- **Type:** SaaS web application
- **Initial goal:** Free user management and access control, with an AI gateway architecture prepared for future credit-based commercialization.

> **Version 2.0 change log:** The business model no longer includes a fixed subscription for BYOK. Commercial usage will be sold as credits consumed through the Manager AI gateway. Devices are telemetry only during the free phase, with no device-count enforcement. Extension integration was moved earlier in the roadmap. See [decisions.md](decisions.md) for details.

---

## 1. Overview

ZetaPilotAI Manager is an independent system for managing users, licenses, and—during the commercial phase—AI usage for the ZetaPilotAI Visual Studio Code extension.

The Manager is separate from the extension's internal code and does not depend on the corporate WordPress site. It has its own backend, frontend, and database. The extension communicates with it through a management REST API and, in the commercial phase, an AI gateway that proxies provider requests.

### 1.1. Value proposition

ZetaPilotAI is derived from Cline, which is free and open source. Commercial viability therefore requires a clear reason for users to pay:

1. **Zero configuration:** Users can sign in and get started without creating provider accounts or managing API keys.
2. **Unified usage dashboard:** Spending, limits, and history in one place, regardless of provider.
3. **Spending controls:** Period limits, alerts, and automatic blocking—features not provided by plain BYOK.
4. **Curated models:** Simple access to selected models without navigating each provider's catalog.

Charging a fixed BYOK fee would compete directly with free Cline. Commercial revenue therefore comes from **AI usage routed through the Manager**, not access to the extension.

## 2. Business model

### Phase 1 — Free (BYOK)

- Users create an account, sign in to the extension, configure AI providers, supply their own API keys, and select models.
- ZetaPilotAI does not provide credits in this phase. Users pay providers directly.
- **Devices are telemetry only** and visible to the user and administrators; there is no device-count limit.
- Every new account automatically receives a free license during registration.

### Phase 2 — Credit-based commercialization

- The Manager operates as an **AI gateway**. The extension sends requests to the Manager, which routes them to providers using server-held master keys, measures usage, and debits the user's credit balance.
- ZetaPilotAI appears as a managed provider in the extension. Users selecting it do not need their own provider key.
- Billing is **pay-as-you-go** through credit purchases. Each model has its own price and provider-cost margin.
- Welcome credits may be offered at commercial launch, including to existing free users.
- BYOK remains free for advanced users who prefer their own keys.

### Phase 3 — Optional future offering

- A monthly plan may include a credit allowance. A subscription that only charges for access to the BYOK extension is out of scope.
- Evaluate this only after Phase 2 is stable and conversion metrics are available.

### Go/no-go criteria and KPIs

Phase 2 may begin only after Phase 1 meets agreed minimums, such as weekly active users, 30-day retention, and NPS or survey results. Define KPI targets in the roadmap before the public Phase 1 launch.

## 3. Components

### ZetaPilotAI Extension

- Installed in Visual Studio Code and provides the user interaction surface.
- Connects directly to AI providers in BYOK mode.
- Connects to the Manager for authentication and license validation, and for AI requests through the gateway in the commercial phase.
- Stores BYOK keys securely in VS Code SecretStorage.

### ZetaPilotAI Manager

- Independent user registration and authentication.
- Device telemetry during the free phase.
- License management, including automatically provisioned free licenses and active, suspended, expired, and canceled states.
- AI gateway, usage metering, and credit ledger in Phase 2.
- Administration, product version management, and audit history.

### WordPress

- Corporate website, product information, documentation, and announcements.
- Directs visitors to registration and the user portal.
- Does not manage users, licenses, or credits.

## 4. Scope

### Included in Phase 1

- User accounts: registration, sign-in, recovery, email verification, and LGPD deletion/anonymization.
- Device registration and lookup without enforcement limits.
- Automatic free licenses and extension validation with an offline grace policy.
- User dashboard and basic admin dashboard.
- Extension-to-Manager sign-in, activation, and validation integration.

### Excluded from Phase 1

- Billing, payments, credits, and the AI gateway (Phase 2). Only architectural seams are prepared: plan entity, license status, and the managed ZetaPilotAI provider concept.
- Device-count enforcement.
- Subscriptions (optional Phase 3).

## 5. Project documents

| Document | Contents |
|---|---|
| [vision.md](vision.md) | Vision, business model, and scope |
| [architecture.md](architecture.md) | Technical architecture, AI gateway, security, data, and API |
| [decisions.md](decisions.md) | Architecture decision records and pending decisions |
| [roadmap.md](roadmap.md) | Development phases, tasks, and exit criteria |

## 6. Permanent guidelines

- Keep this project independent from the extension and WordPress, with its own Git repository.
- Do not reuse Cline authentication, classes, or internal structures.
- Store BYOK keys only in extension SecretStorage. The Manager must never receive, store, or log them.
- Store provider master keys for Phase 2 only in server secrets or environment variables—not in plaintext in the database or in the extension.
- Never log prompt or response contents in the gateway. Store metadata and token counts only, in accordance with LGPD requirements.
- Preserve Apache 2.0 license notices from Cline in the derived product.
- Do not modify the ZetaPilotAI extension while developing the Manager unless explicitly authorized.
- Implement unit and integration tests; document REST endpoints.