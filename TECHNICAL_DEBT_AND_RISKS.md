# Technical Debt and Risks

| Severity | Evidence | Impact | Future treatment |
|---|---|---|---|
| High | Static `Browser`/`Playwright` plus instance `HashMap`s in `BrowserFactory` | Parallel classes may race or share resources | Improve ownership before enabling parallel runs |
| High | `TestConfig` catches load errors and prints a stack trace | Missing configuration can become a later null failure | Fail fast with a clear configuration exception |
| High | `TestDataLoader` calls `props.load(is)` without checking a null stream | Missing data path produces an opaque exception | Validate resource presence and identify path |
| Medium | `BaseTest` and `TestContext` duplicate ThreadLocal metadata | State can diverge and cleanup is incomplete | Consolidate context behind one abstraction |
| Medium | `@AfterEach` desktop capture and several utility catches log and continue | Diagnostic failure may be hidden; Robot is environment-sensitive | Make artifact failure policy explicit |
| Medium | `PlaywrightActions` contains literal waits and broad action surface | Slow/flaky waits and low cohesion | Centralize configured timeout/wait policy |
| Medium | `testFailureIgnore=true` defers failure to shell XML parsing | Build correctness depends on duplicate CI logic | Preserve report-on-failure but use a robust result gate |
| Low | Example credentials are stored in repository resources | Unsafe if reused outside demo data | Replace with injected secrets in real projects |
| Low | Empty page classes, duplicate imports, naming inconsistencies | Maintenance noise | Clean opportunistically, not as architecture changes |

These are observations, not blueprint invariants. A generated framework should
preserve the useful seams while avoiding these implementation weaknesses.
