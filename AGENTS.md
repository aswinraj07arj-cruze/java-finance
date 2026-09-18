# AGENTS.md

## Project
Personal finance tracker — Spring Boot 3, Java 21, Maven.

## Conventions
- Package structure: com.aswin.financetracker.{controller,service,repository,model,dto}
- Use constructor injection, not field injection.
- All monetary values as BigDecimal, never double/float.
- All dates as LocalDate.
- Validate all incoming request DTOs with jakarta.validation annotations.
- Write a unit test for every service method that contains logic (not pure CRUD passthrough).
- Do not commit secrets; use application.properties placeholders + .env for local dev.

## Out of scope for v1
- Multi-user auth, bank syncing, mobile app.
