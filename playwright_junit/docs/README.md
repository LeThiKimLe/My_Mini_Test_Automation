# CI/CD Learning Guide

Read these files in this order:

1. `ci-cd-architecture.md`
   - Start here.
   - Explains the whole system: Maven, JUnit, Playwright, Allure, GitHub Actions, Jenkins.

2. `github-actions-explained.md`
   - Explains `.github/workflows/ci.yml` line by line conceptually.
   - Focuses on jobs, runners, artifacts, and why `if: always()` exists.

3. `jenkins-explained.md`
   - Explains `Jenkinsfile`.
   - Focuses on agents, stages, parameters, Unix/Windows branching, and Allure publishing.

4. `test-tagging-strategy.md`
   - Explains how to tag tests by smoke/regression, sprint, and release.
   - Shows commands for running a sprint or release subset.

Quick commands:

```bash
mvn clean test -Psmoke
mvn clean test -Pregression
mvn clean test "-Dtest.groups=regression & sprint-login"
```

Important output paths:

```text
target/allure-results
target/site/allure-report
target/site/allure-report/data/attachments
target/allure-single/index.html
target/surefire-reports
```
