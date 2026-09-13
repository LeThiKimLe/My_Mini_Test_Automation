# Playwright + TestNG framework

This standalone Maven project preserves the reference architecture:

`Test -> Flow -> Page -> PlaywrightActions -> Playwright`

`BaseTest` owns the class fixture. `BrowserFactory` is the only browser/context
owner, pages keep selectors in locator providers, flows expose business
operations, and configuration/data stay outside scenarios. TestNG lifecycle
methods replace the JUnit callbacks; `FrameworkTestListener` provides suite
cleanup, failure screenshots, metadata, and final report hooks.

## Run

From `playwright_testng`:

```powershell
mvn test
mvn test -Psmoke
mvn test -Pregression -Denv=dev -Dheadless=true
mvn test -Dtestng.suite=src/test/resources/suites/testng.xml
```

Install browsers once with `mvn exec:java` or the Playwright CLI appropriate
to your environment. Test data is a demo property file, not a secret store.
Pass an alternate resource with `-DtestData=testdata/users/admin.properties`.

Allure results are written to `target/allure-results`; use
`mvn allure:serve` (or `mvn allure:report`) after a run. Traces and videos are
kept under `target/test-results/<test-class>`.

TestNG groups are `smoke` and `regression`. Add groups to a test method and
select them using a Maven profile or a custom suite XML.
