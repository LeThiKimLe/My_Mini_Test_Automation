# Coding Conventions

## Observed rules

- Packages are lowercase nouns: `pages`, `flow`, `factory`, `utils`, `tests`.
- Classes are PascalCase with role suffixes: `LoginPage`, `AuthenticationFlow`,
  `BrowserFactory`, `AllureStepScreenshotListener`.
- Test methods are lower camel case and behavior-oriented, for example
  `userCanLoginSuccessfully`.
- Page methods use verbs (`enterUsername`, `clickLogin`, `verifyOnProductPage`);
  locator methods describe the element (`usernameInput`, `loginButton`).
- Constants are uppercase (`REQ`, `SUITE`, `defaultContextName` is an observed
  exception to strict uppercase).
- Constructors inject the action/page dependencies explicitly; there is no DI
  framework.
- Shared state is exposed through protected fields on `BaseTest`, while lifecycle
  and metadata accessors are methods.
- Allure step annotations describe business/UI intent. Logging uses timestamped
  `[time LEVEL] - message` formatting.
- Exceptions are usually propagated from navigation and invalid browser values;
  artifact/report helpers often log and continue. Future code must make that choice
  explicit rather than silently swallowing failures.
- Selector strings stay in locator classes, not in test methods.

## Tags and names

Use lowercase hyphen-separated tags such as `smoke`, `regression`,
`sprint-login`, and `release-1.0`. Composed annotations provide suite tags and
descriptions; native tags provide sprint/release dimensions.

## Style notes

The source uses light comments for lifecycle explanations, minimal JavaDoc, and
some legacy inconsistencies (`WebUI` capitalization, duplicate imports, empty
page classes). These are observations, not requirements for new code.
