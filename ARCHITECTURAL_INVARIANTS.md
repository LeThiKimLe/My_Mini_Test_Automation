# Architectural Invariants

1. **Tests express business scenarios.** Evidence: `LoginTest` calls
   `authenticationFlow.loginAsStandardUser`; preserve this readable boundary.
2. **Pages encapsulate UI interactions.** Evidence: `LoginPage` owns fields/clicks
   and `ProductPage` owns the product assertion; selectors must not leak into tests.
3. **Flows compose reusable business behavior.** Evidence:
   `AuthenticationFlow` coordinates two pages; retain this facade for cross-page
   actions.
4. **Browser creation is centralized.** Evidence: `BaseTest` delegates to
   `BrowserFactory`; tests must not call Playwright launch directly.
5. **Configuration and test data are externalized.** Evidence: `TestConfig` and
   `TestDataLoader`; preserve file/system-property seams.
6. **Observability is framework-level.** Evidence: Allure extensions/listeners and
   `Loggers`; do not require every test to implement screenshot/report plumbing.
7. **Artifacts are associated with test metadata.** Evidence: `TestContext` path
   naming uses REQ, class, case, and screenshot counters.
8. **Runner changes adapt lifecycle APIs, not architecture.** This is the required
   migration interpretation supported by the separation above.

The browser engine, runner annotations, Maven profile syntax, and Allure adapter
are adaptable. Page/Flow/Test responsibilities, centralized factory, config/data
seams, and observability outcomes are not.
