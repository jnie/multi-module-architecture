# AGENTS.md guide

## 1. Repository Overview
- The multi-module-architecture is a multi-module Maven project demonstrating Clean Architecture and Hexagonal Architecture patterns.
- This is to showcase how separation of concerns is kept strict in separate modules, to give a low cognitive experience for humans.
- Low cognitive load is mostly relevant in integrated development environment(IDE). 

**Technology Stack**
- Java 17+
- Spring Boot 3.5.11
- Maven 3.9+
- H2 (in-memory database)
- Spring WebFlux (reactive)

---

## 2. Project Structure, Code style & patterns 

```
multi-module-architecture/
├── app/
│   ├── inbound/api      # REST controllers, DTOs
│   ├── application/     # Spring Boot initializer
│   ├── domain/          # Domain models & interfaces
│   ├── repository/      # Database and infrastructure
│   ├── service/         # Business logic
│   └── outbound/ext.api # External adapters (API clients)
├── doc/                 # Documentation & diagrams
├── AGENTS.md            # Agents guidelines
├── .github/             # GitHub workflows
└── pom.xml              # Parent POM
```

### General principles

1. **Always check existing modules first** - Understand the architecture before making changes
2. **Keep the module boundaries clean** - Don't mix concerns across modules
3. **Domain layer should be vendor-agnostic** - No Spring annotations in domain/
4. **Use Lombok and MapStruct** - Already configured in POMs
5. **main branch** - is off limits, when changing code always create a branch
6. **Run tests before committing** - Use `mvn test`
7. **Port/adapter pattern** - External APIs go in `app/outbound/`, interfaces in `app/domain/`
8. **Port/adapter pattern** - Inbound APIs go in `app/inbound/`, no Interfaces, external parties should rely on 
     OpenAPI documentation, or negotiated message structures for async messaging

### Type safety & Code quality

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

### Formatting
- Use the generic Google Java Style formatting
- Use 2 spaces for indentation.
- Always include curly braces, even for single-line `if` statements.

---

## 3. Agentic workflow (The "How-To")
When you are tasked with a feature or bug fix, follow this exact sequence:

### Phase 1: Exploration & Plan
1. **Search:** Locate relevant logic using `grep` or symbol search.
2. **Propose:** Briefly summarize your plan in the chat before writing code.

### Phase 2: Implementation & Testing
1. **Branch from a clean main** Make sure you have all the latest from main branch, create new branch from here
2. **Execute:** Modify files. Do not delete comments unless they are obsolete.
3. **Local Validation:** - Build command: `mvn clean compile`
  - Test command: `mvn test`
4. **Self-Correction:** If tests fail, analyze the logs, fix the code, and re-run tests until green. **Do not ask for help until you have attempted 2 logical fixes.**

---
### ⚡ Quick Reference

| Action | Command                                                                    |
|--------|----------------------------------------------------------------------------|
| Full build | `mvn clean package`                                                    |
| Run app | `mvn spring-boot:run -pl app/application -Dspring-boot.run.profiles=local`|
| Run tests | `mvn test`                                                              |
| Build specific module | `mvn install -pl <module> -am`                              |
| Check dependencies | `mvn dependency:tree`                                          |
| Default port | 8080, but local is 8081                                              |
| Swagger UI | http://localhost:8081/swagger-ui.html                                  |

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

## 4. Pull Request (PR) requirements

### Communication within the repo
- **Repo:** https://github.com/jnie/multi-module-architecture
- **In chat replies** file references must be repo-root relative only (example: `app/inbound/rest/src/main/java/dk/jnie/example/controllers/MainController.java`); never absolute paths or `~/...`.
- **GitHub PR Summary:** What was changed and why.
- **Breaking Changes:** Explicitly state if any APIs or asset formats were modified. 
- **GitHub issues/comments/PR comments** use literal multiline strings never embed "\\n".
- **GitHub comments** never use `gh issue/pr comment -b "..."` when body contains backticks or shell chars.
- **GitHub linking** don’t wrap issue/PR refs like `#24643` in backticks when you want auto-linking. Use plain `#24643` (optionally add full URL).
- **PR landing comments** always make commit SHAs clickable with full commit links (both landed SHA + source SHA when present).
- **PR review conversations** if a bot leaves review conversations on your PR, address them and resolve those conversations yourself once fixed. Leave a conversation unresolved only when reviewer or maintainer judgment is still needed; do not leave bot-conversation cleanup to maintainers.
- **Security advisory analysis** before triage/severity decisions, read `SECURITY.md` to align with agreed trust model and design boundaries.
- **Risk Assessment:** Label as [Low/Medium/High] risk.

---

## Changelog Release Notes

- When cutting a release with beta GitHub prerelease:
  - Tag `vYYYY.M.D-beta.N` from the release commit (example: `v2026.2.15-beta.1`).
  - Create prerelease with title `mma YYYY.M.D-beta.N`.
  - Use release notes from `CHANGELOG.md` version section (`Changes` + `Fixes`, no title duplicate).
  - Attach at least `MMA-YYYY.M.D.zip` and `MMA-YYYY.M.D.dSYM.zip`.

- Keep top version entries in `CHANGELOG.md` sorted by impact:
  - `### Changes` first.
  - `### Fixes` deduped and ranked with user-facing fixes first.



## 5. Constraints & Boundaries
- **Dependencies:** Do not add new external libraries without explicit user approval.
- **Secrets:** Never commit `.env` files or API keys.
- The project uses **reactive programming** (Reactor) style, keep that
- **NO-GO zone** External Advice Slip API (https://api.adviceslip.com/) is off limits, auto generation of classes are part of build
