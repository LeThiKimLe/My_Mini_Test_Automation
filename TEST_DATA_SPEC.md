# Test Data Specification

The confirmed test-data mechanism is classpath `.properties`. `LoginTest` loads
`testdata/users/admin.properties` in a per-test `@BeforeEach`, then passes values
to a page or flow. Test logic therefore contains keys, not credential literals.
`TestDataLoader` uses the classloader and wraps load failures in
`RuntimeException`.

Data ownership belongs to the test fixture/scenario; `TestDataLoader` performs
loading and returns `Properties`; pages and flows receive already-selected values.
There are no confirmed Excel/JSON providers, builders, fixtures, cleanup routines,
or data-isolation mechanisms beyond resource immutability and test setup.

For a new runner preserve the separation: load/validate data before the scenario,
pass typed or named values into flows/pages, and keep selectors/business behavior
out of data files. If parameterization is introduced, adapt it at the test layer
without moving browser ownership or assertions into a provider.
