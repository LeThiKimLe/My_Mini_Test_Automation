# Browser Management Specification

## Confirmed lifecycle

`BrowserFactory` owns static `Playwright` and `Browser` references. A `BaseTest`
instance lazily obtains one factory, launches one browser, and creates a named
`BrowserContext` and `Page`. The default context is named `Default`. Additional
contexts/pages and persistent guest contexts are supported by public factory
methods. The browser is closed in `@AfterAll`, so the intended lifecycle is
per test class (with shared static process resources), not per individual test.

Each normal context enables ignored HTTPS errors, downloads, null viewport,
video recording, and a 1280x720 video size. Tracing starts on context creation,
captures screenshots and snapshots, and stops into a test-specific trace path.
Browser selection supports chromium, Chrome, Edge, Firefox, and WebKit; unknown
values throw `IllegalArgumentException`.

## Ownership and isolation

`BrowserFactory` maps context names to pages/contexts. `PlaywrightActions` owns the
active page/context pair and can switch pages or contexts. Test metadata,
actions, page objects, factory instances, logs, and video paths use ThreadLocal
where the implementation expects parallel execution. However, static Browser and
Playwright fields and non-thread-safe maps mean full parallel safety is not
confirmed; treat class-level browser ownership and one test thread per factory as
the safe operating assumption.

## Adaptation rules

Keep one central factory, named contexts, a single action abstraction, tracing/video
hooks, and guaranteed teardown. A new runner may replace JUnit lifecycle plumbing
and may improve resource ownership, but must not let individual tests launch
ad-hoc browsers. Storage state, cookies, proxy, and authentication are not
configured in the reference implementation; add them only as explicit extension
points, not assumed behavior.
