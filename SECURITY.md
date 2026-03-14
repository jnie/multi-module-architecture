# Security Policy

## Supported Versions

This is a demonstration project for learning purposes. No formal security support is provided.

| Version | Supported          |
| ------- | ------------------ |
| main    | :white_check_mark: |

## Reporting a Vulnerability

This is an educational/demo project. If you discover a security vulnerability:

1. **Do not open a public issue** - security vulnerabilities should not be disclosed publicly
2. Email the maintainer directly or use GitHub's private vulnerability reporting feature
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if available)

You can expect a response within 1-2 weeks. Note that as this is a demo project, fixes may not be prioritized.

## Scope

### In Scope
- Security issues in the application code
- Vulnerabilities in dependencies (reported via Dependabot)

### Out of Scope
- The external Advice Slip API (https://api.adviceslip.com/) - this is a third-party service
- Denial of service attacks
- Social engineering attacks
- Physical security

## Security Best Practices

When working with this codebase:

- **Never commit secrets** - API keys, passwords, `.env` files should never be committed
- **Use dependency scanning** - Dependabot is enabled; review alerts promptly
- **Keep dependencies updated** - Run `./mvnw dependency:tree` to check for outdated packages
- **Follow OWASP guidelines** - This project follows Clean Architecture principles that naturally separate concerns and reduce attack surface

## Dependencies

This project uses:
- **Spring Boot 3.x** - Active security updates
- **H2 Database** - In-memory only, no persistent data exposure
- **Java 21+** - Modern runtime with security patches

Security patches are applied through regular dependency updates.