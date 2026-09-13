# AI Framework Generation Instructions

Act as an automation framework architect. Generate a framework that preserves the
reference's architecture and design intent, not merely its syntax or annotations.

1. Start with `BaseTest`, a central `BrowserFactory`, `PlaywrightActions`,
   `BasePage`, locator providers, Page Objects, Flow facades, configuration,
   data loading, logging, and report hooks.
2. Keep tests business-readable and make Page -> Flow -> Test the normal dependency
   direction. Pass dependencies through constructors; do not introduce a DI
   container, Cucumber, or an unrelated service layer unless explicitly required.
3. Use role-based names (`*Page`, `*Flow`, `*Factory`, `*Listener`) and lowercase
   hyphenated tags. Keep selectors in locator classes and common operations in the
   action wrapper.
4. Centralize browser/context/page creation and guarantee teardown. Preserve
   tracing, video, screenshots, metadata-based artifact paths, and environment
   report properties.
5. For JUnit to TestNG, map lifecycle and listener APIs while preserving their
   responsibilities and ordering. Use TestNG groups for the suite/sprint/release
   semantics; do not mechanically translate annotations.
6. Keep configuration/data outside scenario logic and make missing resources
   explicit. Do not hard-code URLs, credentials, or selectors in tests.
7. Validate generated code against the consistency checklist and compare object
   creation, lifecycle, dependency direction, report outputs, and CI selection.
8. When requirements are ambiguous, preserve the documented invariant and mark the
   choice as adaptable rather than inventing generic framework features.

Before delivering, verify that every major component has an owner, lifecycle,
extension point, and testable failure behavior. Avoid the common mistake of
producing a generic Page Object framework without the Flow facade and centralized
observability.
