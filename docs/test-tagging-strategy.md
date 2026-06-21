# Test Tagging Strategy

This project uses JUnit 5 tags to select test cases by suite, sprint, and release.

## Why Tags Matter

Each test case can belong to multiple groups:

- test type: `smoke`, `regression`
- sprint: `sprint-login`, `sprint-checkout`, `sprint-2026-06`
- release: `release-1.0`, `release-1.1`

That lets you run exactly the slice you need.

Example:

```bash
mvn clean test "-Dtest.groups=regression & sprint-login"
```

This means:

> Run only tests that are both regression and part of sprint-login.

## Current Example

```java
@SmokeRegressionTest(description = "Verify user can login successfully")
@Tag("sprint-login")
@Tag("release-1.0")
void userCanLoginSuccessfully() {
    ...
}

@RegressionTest(description = "Verify user cannot login with invalid credentials")
@Tag("sprint-login")
@Tag("release-1.0")
void userCannotLoginWithInvalidCredentials() {
    ...
}
```

`@SmokeRegressionTest` already includes:

```text
@Test
@Tag("smoke")
@Tag("regression")
```

`@RegressionTest` already includes:

```text
@Test
@Tag("regression")
```

Sprint and release tags are added with normal JUnit `@Tag`.

This is intentional: JUnit can filter native `@Tag` values reliably during test discovery.

## Recommended Naming

Use lowercase and hyphen-separated names.

Good:

```text
sprint-login
sprint-checkout
sprint-2026-06
release-1.0
release-2.3
```

Avoid:

```text
Sprint Login
Sprint_Login
SPRINT_LOGIN
release 1.0
```

Consistency is more valuable than cleverness here.

## Useful Local Commands

Run all smoke tests:

```bash
mvn clean test -Psmoke
```

Run all regression tests:

```bash
mvn clean test -Pregression
```

Run regression tests from one sprint:

```bash
mvn clean test "-Dtest.groups=regression & sprint-login"
```

Run smoke tests from one sprint:

```bash
mvn clean test "-Dtest.groups=smoke & sprint-login"
```

Run tests from a release:

```bash
mvn clean test "-Dtest.groups=regression & release-1.0"
```

Run tests from either sprint:

```bash
mvn clean test "-Dtest.groups=regression & (sprint-login | sprint-checkout)"
```

Exclude a tag:

```bash
mvn clean test "-Dtest.groups=regression & !flaky"
```

## GitHub Actions

For normal events:

- Pull request: smoke job only
- Push/manual without custom tag expression: smoke and regression jobs

For manual custom runs, use workflow dispatch input:

```text
test_groups = regression & sprint-login
```

When `test_groups` is provided, GitHub Actions skips the default smoke/regression jobs and runs the custom tagged job.

## Jenkins

Jenkins uses a dynamic picker after checkout.

The choices come from:

```text
ci/test-groups.txt
```

Example file:

```text
all
smoke
regression
smoke & sprint-login
regression & sprint-login
regression & release-1.0
```

When Jenkins starts:

1. It checks out the repository.
2. It reads `ci/test-groups.txt`.
3. It shows a dropdown inside the build.
4. The selected value is passed to Maven.

Example:

```text
Selected group = regression & sprint-login
```

Advanced example:

```text
CUSTOM_TEST_GROUPS = regression & (sprint-login | sprint-checkout)
```

If `CUSTOM_TEST_GROUPS` is filled when starting the build, Jenkins skips the picker and runs that expression directly.

## Practical Rule

When adding a new test case, give it at least:

```text
1 suite tag: smoke/regression
1 sprint tag
1 release tag when the release is known
```

In code:

```java
@RegressionTest(description = "...")
@Tag("sprint-checkout")
@Tag("release-1.1")
void testName() {
    ...
}
```
