# Component Specifications

## Base test

`BaseTest` is the fixture/template. Subclasses declare `REQ` and `SUITE` constants
when they want report metadata, inherit protected page/flow/action/logger fields,
and should contain scenario orchestration rather than browser plumbing.

## Base page and pages

`BasePage` receives `PlaywrightActions`, creates a logger, and supplies common
navigation. Concrete pages receive the same action object, expose business-named UI
operations, use locator providers, and annotate meaningful operations with
`@Step`. `LoginPage` owns login inputs/clicks and invalid-login UI verification;
`ProductPage` owns the product-page UI assertion. Empty page shells are placeholders,
not evidence of additional behavior.

## Flows

Flows are reusable business facades. `AuthenticationFlow` receives page objects
through its constructor, composes them, and verifies the resulting business state.
Flows may orchestrate pages and contain business-level verification, but should not
declare selectors, launch browsers, load CI configuration, or become a second test
class.

## Locators and actions

Locator classes expose static selector factories and call `Locators.create`, which
records the declaring page/method name in ThreadLocal metadata. `PlaywrightActions`
wraps navigation, locator lookup, input, click, waits, page switching,
screenshots, authentication routing, and browser interaction. New common actions
belong there rather than being duplicated in pages.

## Utility classes

`TestConfig`/`ConfigReader` are configuration; `TestDataLoader` is resource
property loading; `TestContext` is per-thread report state; `Loggers` bridges
SLF4J and Allure; `Utils` handles artifact paths, screenshots, environment files,
copying, and report invocation; `DateUtils` and `MathUtils` are generic helpers.
No Excel or JSON test-data provider is present; Jackson is used for Allure JSON
post-processing, not as a test-data contract.
