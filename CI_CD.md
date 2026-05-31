# CI/CD

## GitHub Actions

### CI

Workflow: `.github/workflows/ci.yml`

Runs on push, pull request, and manual dispatch.

Main steps:
- Set up Java 17
- Install Playwright Chromium
- Run smoke tests with `mvn -B clean test -Psmoke`
- Run regression tests with `mvn -B clean test -Pregression`
- Upload screenshots from `target/site/allure-report/data/attachments`
- Upload single-file Allure report from `target/allure-single/index.html`

Because this project is configured to keep generating Allure reports even when tests fail, the workflow has a final guard step that reads Surefire XML and fails the job when any test has failures or errors.

Pull requests run smoke tests only. Push and manual runs execute both smoke and regression jobs.

### CD

Workflow: `.github/workflows/allure-pages.yml`

Runs on push to `main` and manual dispatch.

It runs the same test suite, generates Allure HTML, and deploys `target/site/allure-report` to GitHub Pages.

Before using it, enable GitHub Pages in the repository:

1. Open repository Settings.
2. Go to Pages.
3. Set Source to GitHub Actions.

## Jenkins

Pipeline file: `Jenkinsfile`

The Jenkins pipeline:
- Checks out the repository
- Installs Playwright Chromium
- Lets you choose `smoke`, `regression`, or `all` through the `TEST_SUITE` parameter
- Runs `mvn -B clean test -Psmoke` or `mvn -B clean test -Pregression`
- Publishes JUnit reports
- Archives Allure report/results/screenshots/traces
- Publishes Allure report when the Jenkins Allure plugin is installed

Recommended Jenkins plugins:
- Pipeline
- Git
- JUnit
- Allure Jenkins Plugin

Recommended tools on Jenkins agents:
- JDK 17
- Maven 3.9+
- Git

## Single-file Allure report

The normal Allure report is a static folder:

```text
target/site/allure-report
```

For easier sharing, Maven also generates one self-contained HTML file:

```text
target/allure-single/index.html
```

Local command:

```bash
mvn clean test
```

Smoke only:

```bash
mvn clean test -Psmoke
```

Regression only:

```bash
mvn clean test -Pregression
```
