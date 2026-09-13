# Framework Consistency Checklist

- [ ] Single module/build entry point and documented execution commands
- [ ] Role-based package structure (`base`, `factory`, `pages`, `flow`, `locators`,
      `utils`, test extensions)
- [ ] Tests express scenarios rather than raw selectors
- [ ] Page Objects own UI operations and UI assertions
- [ ] Flow classes compose reusable business workflows
- [ ] Browser/context/page creation is centralized
- [ ] Context and page ownership is explicit and teardown is guaranteed
- [ ] Configuration has environment-over-global precedence
- [ ] Test data is externalized and loaded before scenario execution
- [ ] Metadata drives artifact naming and is isolated per test/thread
- [ ] Common actions and waits are not duplicated in pages
- [ ] Allure/report adapter captures descriptions, tags, steps, and attachments
- [ ] Screenshots, video, and traces are retained on normal and failed runs
- [ ] Listener/extension registration is documented
- [ ] Logging reaches both console and report diagnostics
- [ ] Test failures propagate to CI even when reports are generated
- [ ] Smoke/regression/custom selection is supported
- [ ] Runner-specific lifecycle code is isolated from Page/Flow architecture
- [ ] Parallel execution claims match actual resource ownership
- [ ] No direct browser launch, selector leakage, or hidden configuration in tests
- [ ] Technical debt is tracked separately from mandatory architecture

Reviewer result: compare each checkbox against both the original repository and
the generated framework; record deviations as intentional adaptations, not
silent substitutions.
