# Reporting and Observability

## Allure behavior

Allure JUnit 5 integrates test results. `@Step` marks factory, page, flow, and
base-page operations. `AllureDescriptionExtension` copies composed annotation
descriptions into the test result. `AllureStepScreenshotListener` adds JUnit tags
as Allure labels, adds a start timestamp to descriptions, manages step log stacks,
and captures Playwright full-page screenshots around page/flow steps. `BaseTest`
also captures a desktop screenshot after each test. Browser contexts record video
and tracing; paths are moved into result directories during teardown.

`LauncherSessionListener` cleans results at plan start, writes environment
properties at plan end, and generates the report. The service descriptor registers
it. `Utils` additionally normalizes suite names and post-processes Allure JSON.
Maven produces the static report and single-file HTML.

## Independent concepts and adapters

Framework-independent: step semantics, test descriptions/tags, screenshots on
failure/step, trace/video retention, environment metadata, deterministic artifact
directories, and report generation after all tests. Allure-specific: lifecycle
objects, `@Step`, attachments, labels, JSON format, and listener interfaces.
JUnit-specific: `AllureJunit5`, `ExtensionContext`, `TestExecutionListener`, and
ServiceLoader registration. TestNG adaptation should use TestNG listeners/hooks
while retaining the same metadata and artifact outcomes.

SLF4J is the console logging backend. `Loggers` mirrors meaningful messages into
Allure step logs/attachments. Report helpers log many I/O failures; test
navigation/assertion failures remain observable and propagate.
