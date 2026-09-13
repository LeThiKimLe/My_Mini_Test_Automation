# Extension Model

Add a page by extending `BasePage`, accepting the shared `PlaywrightActions`,
adding a dedicated locator provider, and exposing UI operations. Add a flow by
injecting the required pages and composing business actions. Add a test by
extending `BaseTest`, loading resource data, selecting a composed suite annotation,
and adding sprint/release tags.

Add a browser by extending the explicit selection in `BrowserFactory` and its
configuration contract. Add common UI behavior to `PlaywrightActions`; add
cross-cutting report behavior as an Allure/JUnit listener or extension rather than
inside every test. Register ServiceLoader listeners under
`src/test/resources/META-INF/services`.

The stable extension seams are constructors, `BaseTest` lifecycle methods,
`BrowserFactory`, `PlaywrightActions`, locator providers, composed annotations,
`TestConfig`, `TestDataLoader`, and Allure lifecycle listeners. There is no plugin
registry or DI container. New behavior must preserve these seams and dependency
direction.
