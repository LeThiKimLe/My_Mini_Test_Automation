# Framework DNA Blueprint

## A. Framework identity

An intentionally business-readable Java UI automation framework: Page Objects own
UI, Flow facades own reusable business workflows, and tests select scenarios and
data. It favors explicit constructor wiring, a central browser factory, shared
action wrappers, external properties, and listener-driven Allure diagnostics.

## B. Architecture

`Test -> Flow -> Page -> PlaywrightActions -> Playwright`; `BaseTest` owns fixture
creation; `TestConfig`, `TestContext`, `Loggers`, and `Utils` support every layer.
See [ARCHITECTURE_ANALYSIS.md](./ARCHITECTURE_ANALYSIS.md).

## C. Mandatory components

`BaseTest`, `BrowserFactory`, `BasePage`, concrete Page Objects, locator providers,
`PlaywrightActions`, reusable Flow classes, centralized configuration, resource
test-data loader, per-test metadata context, logging, and report/listener hooks.

## D. Optional components

Additional pages/flows, persistent guest contexts, multi-page switching, API
helpers, alternate data sources, and extra CI publishers are optional. They must
not bypass the mandatory boundaries.

## E-F. Naming and coding

Use lowercase role packages, PascalCase role-suffixed classes, behavior-oriented
camelCase methods, lowercase hyphenated tags, constructor dependency passing, and
selectors in locator providers. Annotate business/UI operations as report steps.

## G-I. Test, Page, Flow rules

Tests should arrange data and invoke business behavior. Pages expose UI verbs and
UI assertions; they do not launch browsers or contain suite selection. Flows
compose pages, may verify business outcomes, and do not own selectors or fixture
lifecycle.

## J-K. Browser and configuration

Use one centralized factory, named contexts, per-class setup/teardown as the
reference default, and explicit tracing/video/screenshot lifecycle. Resolve
environment properties first and global properties second; do not hard-code URLs
or credentials in tests.

## L-N. Data, reporting, listeners

Load data before the test and pass values downward. Keep report hooks centralized:
descriptions, tags, steps, screenshots, video, trace, environment metadata, and
final report generation. Adapt listener APIs, not outcomes, when changing runners.

## O-Q. CI, errors, parallelism

CI installs browsers, supports smoke/regression/custom selection, publishes
diagnostics, then fails on actual test results. Propagate test failures; surface
configuration/data errors clearly. Treat current static browser state as
class-serial unless ownership is redesigned; ThreadLocal alone is not proof of
parallel safety.

## R-U. Invariants, adaptation, prohibitions, limitations

Preserve Page -> Flow -> Test intent, centralized browser ownership, external
config/data, and framework-level observability. Adapt runner, browser API,
report adapter, and CI syntax. Forbid direct browser launches in tests, selectors
in scenarios, duplicated common actions, and silent fallback on invalid setup.
Known limitations are recorded in [TECHNICAL_DEBT_AND_RISKS.md](./TECHNICAL_DEBT_AND_RISKS.md).

## Validation

1. **Can another agent generate Playwright + TestNG from this?** Yes: lifecycle
   mapping, component contracts, naming, CI semantics, and checklist are explicit.
2. **Is identity preserved?** Yes: the Page + Flow + Test boundary and centralized
   action/factory/reporting model are required, not replaced with a generic template.
3. **Are JUnit details separated?** Yes: see
   [JUNIT_TO_TESTNG_ADAPTATION.md](./JUNIT_TO_TESTNG_ADAPTATION.md).
4. **Are extension points documented?** Yes: see
   [EXTENSION_MODEL.md](./EXTENSION_MODEL.md).
5. **Are conventions enforceable?** Yes: package, class, method, locator, tag,
   constructor, and dependency rules include repository examples.
6. **Are browser/test lifecycles understood?** Yes: the class-level ownership,
   context/page creation, tracing/video, cleanup, and failure paths are traced.
7. **Are reporting/listeners covered?** Yes: Allure annotations, adapters,
   ServiceLoader listeners, screenshots, logs, traces, videos, and report paths
   are separated by abstraction.
8. **Are debts separate from requirements?** Yes: risks have their own document.
9. **Can the blueprint prevent generic generation?** Yes: it explicitly forbids
   direct launches, selector leakage, duplicated actions, and runner-driven
   architecture changes.
10. **Is major evidence source-backed?** Yes: conclusions identify concrete
    classes, methods, resources, Maven configuration, and CI workflows; unknowns
    are called out instead of inferred.
