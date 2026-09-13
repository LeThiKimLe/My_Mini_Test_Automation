# CI/CD Execution Specification

GitHub Actions installs Java 17, caches Maven, installs Playwright Chromium with
dependencies, runs smoke/regression/custom tag jobs, uploads screenshots and the
single-file Allure report, and performs a final Surefire XML failure check.
Pull requests run smoke; ordinary pushes run smoke and regression; manual runs
can provide a JUnit tag expression. `allure-pages.yml` publishes the static report
to GitHub Pages on `main`.

Jenkins checks out, installs Chromium, lets a parameter select `regression`,
`smoke`, `all`, or `custom`, runs the matching Maven profile/tag expression,
publishes JUnit results, archives Allure artifacts/traces, and optionally invokes
the Jenkins Allure plugin. Unix and Windows command branches are explicit.

The portable execution contract is: install browser dependencies, select tests by
suite/tag, preserve results even after test failures, publish diagnostics, then
fail the job based on test-result XML. Maven profiles are implementation details;
another runner may use TestNG groups, but must retain suite/sprint/release
selection semantics.
