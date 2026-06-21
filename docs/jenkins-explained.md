# Jenkins Explained

This document explains the `Jenkinsfile`.

Jenkins is also a CI system, but it works differently from GitHub Actions.

GitHub Actions runs on GitHub-hosted runners by default. Jenkins usually runs on infrastructure you manage: a local machine, a VM, a server, or Jenkins agents.

## Pipeline Structure

The Jenkinsfile starts with:

```groovy
pipeline {
    agent any
```

This means Jenkins can run the pipeline on any available agent.

An agent is the machine where commands actually execute.

## Options

```groovy
options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '20'))
}
```

Meaning:

- `timestamps()`: add timestamps to the Jenkins log
- `buildDiscarder(...)`: keep only the latest 20 builds

This prevents Jenkins from storing old build logs forever.

## Parameters

```groovy
parameters {
    choice(name: 'TEST_GROUP_PRESET', choices: [...], description: 'Choose a predefined test group expression')
    string(name: 'CUSTOM_TEST_GROUPS', defaultValue: '', description: 'Optional advanced JUnit tag expression')
}
```

When you start a Jenkins build, you can choose:

- `smoke`
- `regression`
- `all`
- `smoke & sprint-login`
- `regression & sprint-login`
- `regression & release-1.0`

For most runs, use `TEST_GROUP_PRESET`.

If you need a tag expression that is not in the dropdown, fill `CUSTOM_TEST_GROUPS`.
When `CUSTOM_TEST_GROUPS` has a value, Jenkins ignores `TEST_GROUP_PRESET`.

## Environment

```groovy
environment {
    MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
}
```

This tells Maven to store dependencies inside the workspace under:

```text
.m2/repository
```

That makes dependency handling more predictable on Jenkins agents.

## Stage: Checkout

```groovy
stage('Checkout') {
    steps {
        checkout scm
    }
}
```

This pulls source code from the repository configured in the Jenkins job.

`scm` means Jenkins uses the repository settings from the job configuration or multibranch pipeline.

## Stage: Install Playwright Chromium

This stage installs Chromium before tests run:

```groovy
mvn -B org.codehaus.mojo:exec-maven-plugin:3.1.0:java \
  -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium"
```

The Jenkinsfile supports both Linux/macOS and Windows agents:

```groovy
if (isUnix()) {
    sh '...'
} else {
    bat '...'
}
```

Use `sh` for Unix-like agents and `bat` for Windows agents.

## Stage: Smoke Tests

```groovy
stage('Smoke Tests') {
    when {
        expression { !params.CUSTOM_TEST_GROUPS?.trim() && params.TEST_GROUP_PRESET == 'smoke' }
    }
    steps {
        runMavenSuite('smoke')
    }
}
```

This stage runs only when:

- `TEST_GROUP_PRESET = smoke`
- and `CUSTOM_TEST_GROUPS` is empty

It calls:

```groovy
runMavenSuite('smoke')
```

That function runs:

```bash
mvn -B clean test -Psmoke
```

## Stage: Regression Tests

```groovy
stage('Regression Tests') {
    when {
        expression { !params.CUSTOM_TEST_GROUPS?.trim() && params.TEST_GROUP_PRESET == 'regression' }
    }
    steps {
        runMavenSuite('regression')
    }
}
```

This stage runs only when:

- `TEST_GROUP_PRESET = regression`
- and `CUSTOM_TEST_GROUPS` is empty

It runs:

```bash
mvn -B clean test -Pregression
```

## Stage: Custom Tagged Tests

This stage runs when the selected preset is a tag expression, such as:

```text
smoke & sprint-login
regression & sprint-login
regression & release-1.0
```

It also runs when `CUSTOM_TEST_GROUPS` is filled.

Examples:

```bash
mvn -B clean test "-Dtest.groups=regression & sprint-login"
mvn -B clean test "-Dtest.groups=regression & (sprint-login | sprint-checkout)"
```

## Stage: All Tests

When `TEST_GROUP_PRESET = all`, Jenkins runs:

```bash
mvn -B clean test
```

This runs the full test set without a tag filter.

## Helper Function: runMavenSuite

```groovy
void runMavenSuite(String suite) {
    if (isUnix()) {
        sh "mvn -B clean test -P${suite}"
    } else {
        bat "mvn -B clean test -P${suite}"
    }
}
```

This keeps the pipeline DRY. Without this helper, the same Unix/Windows branching would be repeated in both smoke and regression stages.

## Allure Publishing

Inside each test stage, Jenkins calls:

```groovy
allure([
    commandline: 'allure',
    includeProperties: false,
    results: [[path: 'target/allure-results']]
])
```

This uses the Jenkins Allure plugin.

The input is:

```text
target/allure-results
```

That folder contains the raw Allure result JSON and attachment files.

Jenkins then renders an Allure report in the Jenkins UI.

## Maven Still Generates Reports

Even though Jenkins publishes Allure, Maven also generates:

```text
target/site/allure-report
target/allure-single/index.html
```

This happens because `pom.xml` binds Allure report generation to the Maven `test` phase.

So when Jenkins runs:

```bash
mvn -B clean test -Psmoke
```

Maven does more than just run tests:

```text
clean target
compile code
run filtered tests
generate Allure folder report
generate Allure single-file report
```

## Jenkins Vs GitHub Actions

| Topic | GitHub Actions | Jenkins |
| --- | --- | --- |
| Runner/agent | Usually GitHub-hosted | Usually your own infrastructure |
| Config file | `.github/workflows/ci.yml` | `Jenkinsfile` |
| Manual input | `workflow_dispatch` | Build parameters |
| Test suite split | Separate jobs | Parameter-controlled stages |
| Report publishing | Artifacts | Jenkins Allure plugin |

## Mental Model

Think of Jenkins as a programmable control room.

GitHub Actions says:

```text
An event happened, so I will run a predefined workflow.
```

Jenkins says:

```text
Someone started or scheduled a job, so I will run the selected pipeline path.
```

In this project, both systems eventually do the same important thing:

```bash
mvn clean test -Psmoke
mvn clean test -Pregression
```

The rest is orchestration and reporting.

## Common Jenkins Problems

### Allure step is not recognized

Install the Jenkins Allure plugin and configure an Allure commandline tool named `allure`.

### Browser install fails

Make sure the Jenkins agent has permission to install Playwright browsers and has network access.

### Tests run locally but fail in Jenkins

Check:

- agent OS
- Java version
- Maven version
- browser installation
- whether the app/test URL is reachable from the Jenkins agent

### Report exists in workspace but not in Jenkins UI

Check the Allure plugin configuration and confirm this folder exists:

```text
target/allure-results
```
