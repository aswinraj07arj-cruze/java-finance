# Deployment Checklist (Render & PostgreSQL)

This checklist outlines the configuration and verification steps required for deploying the Finance Tracker application to Render.

---

## 1. Required Environment Variables

Configure the following environment variables in the Render Dashboard (under **Environment** in your Web Service settings):

| Variable Name | Required Value / Format | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` | Activates production profile with PostgreSQL, Flyway, and strict schema validation. |
| `LEGACY_OWNER_EMAIL` | e.g. `aswin@example.com` | Email address entitled to claim and activate the legacy database records upon registration. |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:5432/<database>` | JDBC connection URL for the production PostgreSQL database. |
| `SPRING_DATASOURCE_USERNAME` | `<username>` | PostgreSQL database username. |
| `SPRING_DATASOURCE_PASSWORD` | `<password>` | PostgreSQL database password. |

---

## 2. Render Database URL Conversion

Render provides a default PostgreSQL connection string in the following format:
```text
postgres://user:pass@host:5432/db
```

Spring Boot JDBC requires standard `jdbc:postgresql://` format with username and password provided separately. Split Render's string into the three variables as follows:

- **Render Connection String**:
  `postgres://postgres_user:SecretPassword123@dpg-c123456789-a.oregon-postgres.render.com:5432/finance_db`
- **Converted Environment Variables**:
  - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://dpg-c123456789-a.oregon-postgres.render.com:5432/finance_db`
  - `SPRING_DATASOURCE_USERNAME`: `postgres_user`
  - `SPRING_DATASOURCE_PASSWORD`: `SecretPassword123`

### SSL Configuration Note
- **External Connections**: If connecting to Render PostgreSQL from outside Render (or via external hostname), append `?sslmode=require`:
  ```text
  SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>?sslmode=require
  ```
- **Internal Connections**: Internal Render connections (using the internal database hostname within the same Render region) communicate securely over Render's private network and do not need `?sslmode=require`.

---

## 3. Health Check Path

Render requires a health check endpoint to determine if the web service is alive and healthy before routing traffic.

- **Health Check Path**: `/actuator/health`
- **Expected Response**: HTTP 200 OK with JSON body:
  ```json
  {"status":"UP"}
  ```

Configure `/actuator/health` as the **Health Check Path** in Render Service Settings.

---

## 4. Flyway Database Migrations on Startup

When `SPRING_PROFILES_ACTIVE=prod` is set:
- `spring.flyway.enabled=true`
- `spring.flyway.baseline-on-migrate=true`
- `spring.flyway.baseline-version=0`
- `spring.jpa.hibernate.ddl-auto=validate`

Flyway will automatically baseline an existing production database at version 0, execute idempotent migrations `V1__init_schema.sql` and `V2__add_user_id_columns_and_migrate.sql` without data loss, and Hibernate will validate entity-schema alignment on startup.
