# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Changes

- **Dependency management**: Consolidated ArchUnit version to parent POM
  - Added `<archunit.version>1.3.0</archunit.version>` property in parent `pom.xml`
  - Added `archunit-junit5` to `<dependencyManagement>` section
  - Removed explicit version from `app/architecture-tests/pom.xml`
  - Ensures all dependency versions are centrally managed

- **Java upgrade**: Upgraded from Java 17 to Java 21
  - Updated `pom.xml` java.version property
  - Updated GitHub Actions workflow to use JDK 21
  - Updated documentation references in README.md, AGENTS.md, SECURITY.md

- **AGENTS.md**: Corrected module structure to match actual project layout
  - Updated from incorrect `inbound/api`, `outbound/ext.api` to actual `inbound/rest`, `inbound/consumer`, `outbound/advice-slip-api`, `outbound/publisher`
  - Added missing `architecture-tests` module to documentation
  - Added `SECURITY.md` to file listing
  - Consolidated duplicate "Changelog Release Notes" sections
  - Fixed incomplete sentence ending
  - Standardized commands to use `./mvnw` consistently

- **README.md**: Improved accuracy and consistency
  - Removed stale Micronaut references from purpose section
  - Updated purpose to reflect framework-agnostic domain design
  - Updated module structure tree to include `architecture-tests`
  - Added `architecture-tests` to module responsibilities table
  - Fixed typo "outboudn" → "outbound"
  - Standardized commands to use `./mvnw` consistently

- **SECURITY.md**: Created new security policy file
  - Documented supported versions
  - Added vulnerability reporting process
  - Defined in-scope and out-of-scope items
  - Included security best practices for contributors
  - Listed dependency security information