# JUnit to TestNG Adaptation

| Current implementation | Invariant concept | JUnit-specific? | TestNG adaptation |
|---|---|---:|---|
| `@BeforeAll` / `@AfterAll` | class/browser resource lifecycle | Yes | `@BeforeClass` / `@AfterClass` |
| `@BeforeEach` / `@AfterEach` | per-test setup and cleanup | Yes | `@BeforeMethod` / `@AfterMethod` |
| `@Test` composed annotations | discoverable scenario | Yes | `@Test` meta strategy or explicit annotation |
| `@Tag` and Maven `test.groups` | suite/sprint/release selection | Yes | TestNG groups and `groups`/XML parameters |
| `TestInfo` | current test display metadata | Yes | `ITestResult`/`ITestContext` |
| JUnit extensions | lifecycle decoration | Yes | `IInvokedMethodListener`, `ITestListener`, `ISuiteListener` |
| JUnit Platform listener | plan start/finish reporting | Yes | `IExecutionListener` or suite listener |
| `AllureJunit5` | runner-to-Allure integration | Yes | Allure TestNG adapter |
| assertion APIs | UI/business verification | No | TestNG assertions or Playwright assertions |
| ThreadLocal context | parallel-safe execution state | No | Preserve, review ownership and cleanup |
| BrowserFactory/Page/Flow layers | architecture and responsibilities | No | Keep unchanged conceptually |

Do not mechanically rename annotations. Preserve setup ordering, metadata
initialization before artifact paths, class-level browser ownership, screenshot/
trace/video behavior, tag semantics, and report finalization. TestNG XML/profile
syntax may replace Maven JUnit filtering. Retry is not implemented in the source;
do not introduce it merely because TestNG supports it.
