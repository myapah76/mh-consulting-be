# MH CONSULTING Backend

Spring Boot modular-monolith backend for the MH CONSULTING React website. It uses Java 21, Maven, PostgreSQL, Flyway, server-side administrator sessions, CSRF protection, and post-commit consultation email notifications.

## Run locally with Docker

1. Copy `.env.example` to `.env` and replace every placeholder password.
2. Run `docker compose --env-file .env up --build`.
3. Health: `http://localhost:8080/actuator/health`
4. Swagger UI: `http://localhost:8080/swagger-ui.html`

Swagger UI exposes separate **Public API** and **Administrator API** definitions. Before using login or another mutating operation through "Try it out", call `GET /api/auth/csrf`; Swagger UI is configured to forward the resulting `XSRF-TOKEN` automatically. Set `SWAGGER_ENABLED=false` to disable both the UI and generated OpenAPI documents in a deployment.

The Compose file deliberately requires database and administrator secrets; it does not contain production credentials. The initial admin initializer is idempotent and only creates the configured email when it does not already exist.

Database configuration uses `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD`. An explicit `DB_URL` such as `jdbc:postgresql://localhost:5432/mh_consulting` can still be supplied as an override.

For Gmail SMTP, use port `587` with `SMTP_ENABLE_SSL=true` (STARTTLS), `SMTP_IMPLICIT_SSL=false`, and a Google App Password in `SMTP_PASSWORD`. `SMTP_FROM_NAME` controls the sender name shown to recipients. Keep the real password only in the ignored local `.env` file or the deployment platform's secret manager.

## Maven

The project targets Java 21:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

For source-level checks on an older JDK 17 workstation only, use `.\mvnw.cmd -Plocal-jdk17 test`. Production and Docker builds remain Java 21.

## SPA authentication and CSRF flow

All frontend requests should use `credentials: 'include'`.

1. Call `GET /api/auth/csrf` and retain the returned token. Spring also sets the `XSRF-TOKEN` cookie.
2. For every `POST`, `PUT`, `PATCH`, or `DELETE`, send the token in the returned header name (normally `X-XSRF-TOKEN`).
3. Login with `POST /api/auth/login` using `{ "email": "...", "password": "..." }`.
4. Read the signed-in administrator with `GET /api/auth/me`; logout with `POST /api/auth/logout`.

The session identifier is stored in the HttpOnly `MHCONSULTING_SESSION` cookie. Configure `FRONTEND_URL` as a comma-separated allow-list if more than one SPA origin is used. In production use HTTPS and `SESSION_COOKIE_SECURE=true`.

## Administrator password reset

The anonymous password-reset endpoints remain CSRF-protected and use `PASSWORD_RESET_URL` to build the link sent by email. For production, configure it as the exact frontend page, for example `https://mh-consulting-five.vercel.app/admin/reset-password`. Reset tokens expire after 30 minutes, are single-use, and only their SHA-256 hashes are stored.

Changing or resetting a password does not invalidate existing authenticated sessions because this project currently uses container-managed HTTP sessions without a global session registry. Existing sessions remain valid until logout or expiry. If global revocation becomes required, add a session-version check or Spring Session-backed session management as a separate, complete change.

## Frontend-compatible API contract

- Public service responses retain `id`, `slug`, `title`, `category`, `shortDesc`, `icon`, `fullContent`, `detailedPoints`, `benefits`, and `processSteps`.
- Categories are serialized as `thanh-lap`, `ke-toan`, `thue`, and `khac`.
- Consultation creation accepts canonical `customerName` and the existing frontend alias `fullName`.
- Stored page/service content accepts plain text or Markdown and rejects HTML tags to prevent stored XSS.
- Public service detail only returns active services. Referenced services are deactivated instead of physically deleted.

Pagination responses use Spring's standard `Page` JSON shape. List endpoints cap `size` at 100 and restrict sortable fields.

## Modules

Each business feature owns its controller, DTOs, entity, repository, service, and mapper. Consultation-to-service lookups and deletion-reference checks use public service interfaces; modules never access another module's repository.

- `auth`
- `servicecatalog`
- `pagecontent`
- `contact`
- `consultation`
- `shared`

Flyway migration `V1` creates all schema objects and indexes. `V2` idempotently seeds the exact eight frontend services and their ordered child content, plus the website contact record and initial editable sections. The removed VAT/PIT refund services are not present.
