# GitHub Actions Explained

This document explains `.github/workflows/ci.yml`.

GitHub Actions is the CI system hosted by GitHub. It runs workflows when something happens in the repository, such as a push, a pull request, or a manual trigger.

## Workflow Trigger

The workflow starts from this block:

```yaml
on:
  push:
    branches:
      - main
      - master
      - develop
  pull_request:
  workflow_dispatch:
```

Meaning:

- `push`: run when code is pushed to `main`, `master`, or `develop`
- `pull_request`: run when a pull request is opened or updated
- `workflow_dispatch`: allow manual runs from the GitHub UI

## Jobs

The workflow has two jobs:

```yaml
jobs:
  smoke:
  regression:
```

Each job gets its own fresh GitHub runner. They do not share the same `target` folder.

That means smoke and regression are isolated from each other. Smoke cannot accidentally reuse files from regression, and regression cannot accidentally reuse files from smoke.

## Smoke Job

Smoke job:

```yaml
smoke:
  name: Smoke tests
  runs-on: ubuntu-latest
```

It runs on Ubuntu because browser automation is reliable and cheap to run there.

The important command is:

```yaml
run: mvn -B clean test -Psmoke
```

Breaking it down:

- `mvn`: run Maven
- `-B`: batch mode, better for CI logs
- `clean`: delete old `target`
- `test`: run the Maven test phase
- `-Psmoke`: activate the Maven smoke profile

The smoke Maven profile tells Surefire to run only JUnit tests tagged as `smoke`.

## Regression Job

Regression job:

```yaml
regression:
  name: Regression tests
  runs-on: ubuntu-latest
  if: github.event_name != 'pull_request'
```

The `if` line means regression does not run on pull requests.

That is useful because PR checks should be quick. Smoke gives fast feedback. Regression can run on push or manual runs.

The important command is:

```yaml
run: mvn -B clean test -Pregression
```

The regression Maven profile tells Surefire to run only tests tagged as `regression`.

## Common Step: Checkout

```yaml
- name: Checkout
  uses: actions/checkout@v4
```

This downloads the repository into the runner.

Without checkout, the runner is an empty machine and Maven has no `pom.xml` to run.

## Common Step: Set Up Java

```yaml
- name: Set up Java
  uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: '17'
    cache: maven
```

This installs Java 17 and enables Maven dependency caching.

The project compiles Java source as version 11, but it is fine to run Maven with JDK 17.

The Maven cache makes later runs faster because dependencies do not need to be downloaded again every time.

## Common Step: Install Playwright Chromium

```yaml
run: >
  mvn -B org.codehaus.mojo:exec-maven-plugin:3.1.0:java
  -Dexec.mainClass=com.microsoft.playwright.CLI
  -Dexec.args="install --with-deps chromium"
```

This uses Playwright Java's CLI to install Chromium and required Linux dependencies.

Why this step exists:

- GitHub runners do not automatically have the Playwright browser installed for Java projects.
- Tests call `playwright.chromium()`, so Chromium must exist before tests run.

## Artifact: Screenshots

```yaml
path: target/site/allure-report/data/attachments
```

Step screenshots are attached to Allure first. After Allure generates the HTML report, those images are copied into:

```text
target/site/allure-report/data/attachments
```

That is why the workflow uploads this folder instead of `target/screenshots`.

`target/screenshots` is only used by the failure watcher in some cases. The Allure attachments folder is the consistent source for step screenshots.

## Artifact: Single-File Allure Report

```yaml
path: target/allure-single/index.html
```

The Maven lifecycle creates this file automatically during `mvn clean test`.

The normal Allure report is a folder:

```text
target/site/allure-report
```

The single-file report is:

```text
target/allure-single/index.html
```

Use the single-file report when you want one portable file to download and open.

## Why `if: always()` Is Used

Artifact steps use:

```yaml
if: always()
```

This means GitHub should try to upload screenshots and reports even if tests failed.

That matters because the report is most useful when tests fail. Without `if: always()`, GitHub might skip report upload exactly when you need it.

## Why There Is A Final Failure Step

At the end of each job:

```bash
if grep -R -E 'failures="[1-9][0-9]*"|errors="[1-9][0-9]*"' target/surefire-reports/*.xml; then
  exit 1
fi
```

This checks the Surefire XML. If any test failed or errored, the job is marked failed.

This is needed because Maven is configured to continue after test failure so Allure reports can still be generated.

## Mental Model

Think of GitHub Actions as a disposable machine factory.

For each job, GitHub gives you a clean machine:

```text
clean machine
  -> checkout code
  -> install Java
  -> install browser
  -> run Maven
  -> upload evidence
  -> throw machine away
```

Nothing persists unless you upload it as an artifact.
