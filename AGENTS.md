# AGENTS.md guide

This is a Spring Boot multi-module Maven project demonstrating Clean Architecture and Hexagonal Architecture patterns.

# Repository Guidelines

- Repo: https://github.com/jnie/multi-module-architecture
- In chat replies, file references must be repo-root relative only (example: `app/inbound/rest/src/main/java/dk/jnie/example/controllers/MainController.java`); never absolute paths or `~/...`.
- GitHub issues/comments/PR comments: use literal multiline strings or `-F - <<'EOF'` (or $'...') for real newlines; never embed "\\n".
- GitHub comment footgun: never use `gh issue/pr comment -b "..."` when body contains backticks or shell chars. Always use single-quoted heredoc (`-F - <<'EOF'`) so no command substitution/escaping corruption.
- GitHub linking footgun: don’t wrap issue/PR refs like `#24643` in backticks when you want auto-linking. Use plain `#24643` (optionally add full URL).
- PR landing comments: always make commit SHAs clickable with full commit links (both landed SHA + source SHA when present).
- PR review conversations: if a bot leaves review conversations on your PR, address them and resolve those conversations yourself once fixed. Leave a conversation unresolved only when reviewer or maintainer judgment is still needed; do not leave bot-conversation cleanup to maintainers.
- GitHub searching footgun: don't limit yourself to the first 50 issues or PRs when wanting to search all. Unless you're supposed to look at the most recent, keep going until you've reached the last page in the search
- Security advisory analysis: before triage/severity decisions, read `SECURITY.md` to align with agreed trust model and design boundaries.

## Project Structure & Module Organization

```
multi-module-architecture/
├── app/
│   ├── inbound/      # REST controllers, DTOs
│   ├── application/ # Spring Boot initializer
│   ├── domain/       # Domain models & interfaces
│   ├── service/      # Business logic
│   └── outbound/     # External adapters (API clients)
├── doc/              # Documentation & diagrams
├── .github/          # GitHub workflows
└── pom.xml           # Parent POM
```

## When working on this project:

1. **Always check existing modules first** — Understand the architecture before making changes
2. **Keep the module boundaries clean** — Don't mix concerns across modules
3. **Domain layer should be vendor-agnostic** — No Spring annotations in domain/
4. **Use Lombok and MapStruct** — Already configured in POMs
5. **Run tests before committing** — Use `mvn test`
6. **Port/adapter pattern** — External APIs go in `app/outbound/`, interfaces in `app/domain/`

### ⚡ Quick Reference

| Action | Command |
|--------|---------|
| Full build | `mvn clean package` |
| Run app | `mvn spring-boot:run -pl app/application -Dspring-boot.run.profiles=local` |
| Run tests | `mvn test` |
| Build specific module | `mvn install -pl <module> -am` |
| Check dependencies | `mvn dependency:tree` |
| Default port | 8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |

### 🔧 Common Issues & Solutions

**Problem:** "Unable to find main class"
```bash
# Make sure to run from application module
mvn spring-boot:run -pl app/application
```

**Problem:** MapStruct/Lombok conflicts
```bash
# Ensure annotation processor order is correct (already configured in pom.xml)
# If issues persist, run:
mvn clean compile
```

**Problem:** Port already in use
```bash
# Kill process on port 8081
lsof -ti:8081 | xargs kill -9
# Or run on different port
mvn spring-boot:run -Dspring-boot.run.arguments='--server.port=8082'
```

## Coding Style & Naming Conventions

### Type Safety & Code Quality

- Language: Java 17+; use strict typing; avoid raw types and unchecked casts.
- Formatting: `./mvnw format` (or project formatter); run before commits.
- Verification: `./mvnw verify` runs all checks; fix violations, don't suppress.
- Never use `@SuppressWarnings` without justification; fix root causes instead.
- Never use reflection or runtime bytecode manipulation to share class behavior; use inheritance/composition.
- If reflection is needed, stop and get explicit approval; prefer interfaces and DI.
- In tests, use Mockito `@Mock` per-instance stubs; avoid static state mutation.
- Add brief comments for tricky logic (explain WHY, not WHAT).
- Keep files under ~500 LOC; extract helpers instead of "V2" copies.
- MapStruct: use interfaces with `componentModel = "spring"`; don't manually implement mappers.
- Lombok: avoid `@Data` on JPA entities; prefer `@Value`, `@Builder`, `@RequiredArgsConstructor`.
- Reactive: avoid `.block()` in WebFlux code paths; embrace reactive patterns.
- Naming: follow Spring Boot conventions (`@Service`, `@Repository`, `@RestController`).


## Testing Guidelines

### Test Framework & Coverage
- Framework: JUnit 5 (Jupiter) with Mockito and AssertJ.
- Coverage: JaCoCo thresholds (config in parent pom.xml); run `./mvnw test` before commits.
- Unit tests: `*Test.java` suffix; integration tests: `*IT.java` suffix.
- Run `./mvnw test` (or `./mvnw verify` for integration tests) before pushing when you touch logic.

### Test Execution & Performance
- Do not set Surefire/Failsafe forkCount above 6; tried already.
- If local test runs cause memory pressure, use: `MAVEN_OPTS="-Xmx1g -XX:MaxMetaspaceSize=512m" ./mvnw test`.
- Test profiles: use `-Dspring.profiles.active=test` for test configuration.

### Live Tests (Real APIs/Keys)
- Live tests: `@Profile("live")` annotation; run with `./mvnw test -Dspring.profiles.active=live`.
- Full integration test docs: `doc/testing.md` (if exists) or project README.

### Test Patterns & Best Practices
- Mocking: Use Mockito `@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`.
- Integration tests: `@SpringBootTest`, `@AutoConfigureTestDatabase`, Testcontainers for external deps.
- Reactive tests: Use `StepVerifier` for WebFlux code paths.
- Assertions: Prefer AssertJ fluent assertions (`assertThat(...)`).
- Method naming: Use `@DisplayName("descriptive test name")` for clarity.

### Changelog Release Notes
- User-facing changes only; no internal/meta notes (version alignment, release process).
- Changelog placement: in the active version block, append new entries to the end of the target section (`### Changes` or `### Fixes`).
- Changelog attribution: use at most one contributor mention per line; prefer `Thanks @author`.
- Pure test additions/fixes generally do **not** need a changelog entry unless they alter user-facing behavior.

---

## Technology Stack

- Java 17+
- Spring Boot 3.5.11
- Maven 3.9+
- H2 (in-memory database)
- Spring WebFlux (reactive)

## Changelog Release Notes

- When cutting a release with beta GitHub prerelease:
  - Tag `vYYYY.M.D-beta.N` from the release commit (example: `v2026.2.15-beta.1`).
  - Create prerelease with title `mma YYYY.M.D-beta.N`.
  - Use release notes from `CHANGELOG.md` version section (`Changes` + `Fixes`, no title duplicate).
  - Attach at least `MMA-YYYY.M.D.zip` and `MMA-YYYY.M.D.dSYM.zip`.

- Keep top version entries in `CHANGELOG.md` sorted by impact:
  - `### Changes` first.
  - `### Fixes` deduped and ranked with user-facing fixes first.


## Notes

- The project uses **reactive programming** (Reactor)
- External API: Advice Slip API (https://api.adviceslip.com/)
- Default port: 8081
