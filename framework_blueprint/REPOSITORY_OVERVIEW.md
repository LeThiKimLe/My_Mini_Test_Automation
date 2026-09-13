# Repository Overview

## Scope and evidence

This is a single Maven module (`com.example:playwright_mini`) targeting Java 11. The
repository is an executable UI automation framework, not a production application.
The reference implementation uses Playwright Java 1.57, JUnit Jupiter 5.11, SLF4J,
Allure, AspectJ, Jackson, and Commons IO. The repository structure, `pom.xml`, source
files, test resources, CI files, and existing documentation were inspected. Claims
below are classified as **confirmed** unless marked otherwise.

## Structure

```text
src/main/java
  config/       TestConfig, ConfigReader
  factory/      BrowserFactory
  flow/         AuthenticationFlow
  locators/     LoginLocators, ProductLocators, CommonLocators
  pages/        BasePage and page objects
  utils/        PlaywrightActions, Loggers, TestContext, Utils, report helpers
src/test/java
  base/         BaseTest and JUnit plan listener
  annotations/  suite annotations and Allure description extension
  config/       TestDataLoader
  listener/     Allure/Playwright observability listener
  tests/        scenario tests
src/main/resources
  config-*.properties
src/test/resources
  testdata/ and META-INF/services registrations
.github/workflows  CI and GitHub Pages publishing
ci/                 documented Jenkins test expressions
docs/               CI and tagging guidance
```

The module is layered rather than feature-foldered. `tests` depend on flows/pages,
flows depend on pages, pages depend on the UI action abstraction, and framework
utilities/configuration support all layers. Locator classes are separate from pages.

## Entry points

Local and CI execution enters through Maven Surefire (`mvn clean test`, `-Psmoke`,
`-Pregression`, or `-Dtest.groups=...`). JUnit discovers methods through composed
annotations. `LauncherSessionListener` is discovered by ServiceLoader. `BaseTest`
is the test lifecycle entry point; `AllureJunit5` and
`AllureStepScreenshotListener` are class extensions.

## Reporting entry points

Surefire writes XML under `target/surefire-reports`; Allure adapters write
`target/allure-results`. JUnit plan completion calls `Utils.generateAllureResult()`
and Maven Allure creates `target/site/allure-report` plus the configured single HTML
output. CI archives reports, attachments, traces, and screenshots and deliberately
fails after report generation by inspecting Surefire XML.

## Architectural identity

The explicit identity is business-readable tests: Page Objects encapsulate UI
interaction, Flow objects orchestrate reusable business behavior, and tests compose
those flows. Properties files own environment/test data rather than test methods.
The design is reusable by preserving those responsibilities while adapting runner
integration and Playwright-specific APIs.
