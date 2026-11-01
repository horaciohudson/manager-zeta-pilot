# ZetaPilotAI Manager

User, license, and AI credit management platform for the ZetaPilotAI VS Code extension.

## Documentation

Project documentation is available in [`Project/`](Project/):

- [vision.md](Project/vision.md) — product vision, business model (free BYOK → credits), and scope
- [architecture.md](Project/architecture.md) — technical architecture, security, data, and API
- [decisions.md](Project/decisions.md) — architecture decision records and pending decisions
- [roadmap.md](Project/roadmap.md) — development phases and tasks

## Project structure

```
Backend/    Spring Boot 3.3.3 (Java 17, Maven) — geo-limites conventions
Frontend/   React + TypeScript + Vite
Project/    project documentation
```

The backend package is `com.zetapilot.manager`, organized into layers (`config/`,
`controller/`, `dto/`, `enums/`, `exception/`, `mapper/`, `model/`, `security/`,
`service/`, and `util/`). Flyway migrations are located in
`Backend/src/main/resources/db/migration/`. The frontend uses React, TypeScript,
Vite, and area-specific CSS in `Frontend/src/`.

### Running from IntelliJ IDEA

Set these environment variables in the backend run configuration:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/zetapilot_manager_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
JWT_SECRET=<random secret of at least 32 bytes>
SETTINGS_ENCRYPTION_KEY=<separate random secret of at least 32 bytes>
ADMIN_EMAIL=<initial administrator email>
ADMIN_PASSWORD=<strong password of at least 12 characters>
```

The first-admin bootstrap runs only when `tab_users` is empty. Do not commit real credentials or secrets.

## Development

```bash
Copy-Item .env.docker.example .env.docker
# Before starting, replace JWT_SECRET with a random value of at least 32 bytes.
docker compose --env-file .env.docker up --build
```

To generate a local secret, run `openssl rand -hex 32` and copy the result to
`JWT_SECRET` in `.env.docker`. The backend rejects missing or short secrets.

- Frontend: http://localhost:3000
- Backend: http://localhost:8080 (health: `/actuator/health`, docs: `/swagger-ui.html`)
- PostgreSQL: localhost:5433

The initial health check endpoint is `GET /api/health`; Actuator exposes
`/actuator/health`. This release provides the infrastructure and initial screen.
Additional authentication, refresh-token, and configurable SMTP work is tracked
in `Project/roadmap.md` and the pending decisions in `Project/decisions.md`.
