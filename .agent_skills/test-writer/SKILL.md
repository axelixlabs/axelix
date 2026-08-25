---
name: test-writer
description: Writes new tests for Axelix source code (Java, Kotlin, TypeScript, JavaScript) that follow the project's testing standards — public-API contract coverage, test isolation, given/when/then structure, parameterization, positive + negative cases, and exception-type-only assertions. Use whenever the user asks to write, add, or generate tests for a class/method/module/endpoint, to cover a new feature or bug fix with tests, to "test this", to increase coverage for a specific unit, or when a task involves producing test files under `master/`, `sbs/`, `common/`, or `front-end/` — even if the user only says "add tests for X".
---

# Test Writer

Guide for AI agents **writing new tests** for the Axelix monorepo (`master/`, `sbs/`, `common/`, `front-end/`).

These standards come from how Axelix reviews tests (see the companion `test-reviewer` skill). Writing to them the first time avoids the review churn. The whole point of a test is to pin down the **contract** of a unit — what it promises callers — so that a future change that breaks the promise fails loudly. Everything below serves that goal.

## Before writing a single test

1. **Find the unit under test and its contract.** The contract is the public API, not the implementation:
   - **Java/Kotlin:** `public` methods, ideally declared on an interface with Javadoc. Read the Javadoc — it is the spec.
   - **TypeScript/JavaScript:** functions, classes, hooks, or components **exported** from the module.
2. **Extract the contract into a checklist** before writing any assertions. From the Javadoc/TSDoc and signatures, list:
   - each valid input domain and its success output,
   - every documented exception/error and the condition that triggers it,
   - boundary/edge inputs (empty, `null`, blank, zero, first/last),
   - any implementation-declared guarantee (caching, idempotency, concurrency) stated in the class-level docs.
3. **Turn each checklist item into at least one test.** A happy path alone is an incomplete test suite — the negative cases are where bugs hide.
4. **Locate the right test file.** Naming is `<SourceName>Test` in the **same Gradle/npm module** as the source, mirroring the source package. Match the surrounding tests' style, imports, and assertion library — read a neighbor first.

## The eight standards

### 1. Test the public API only

Test the contract, never the internals. Assert observable outcomes — return values, thrown exceptions, interactions with collaborators — not private fields or private methods.

- Do not use reflection or `@VisibleForTesting` to reach into internals.
- Do not re-implement production logic in the test to "check" it.
- When a concrete class declares an extra guarantee beyond its interface (e.g. a `CachingAuthorityResolver` caches results), test **that** guarantee through the public API — e.g. inject a mock delegate and assert it is called only once for a repeated key.

### 2. Positive **and** negative coverage

Every documented behavior needs a test, both success and failure. Derive them from the contract. For example, given:

```java
/**
 * Parses the given JWT token and converts it into a {@link User}.
 *
 * @throws ExpiredJwtTokenException if the JWT token has expired
 * @throws InvalidJwtTokenException if the JWT token is invalid or tampered with
 * @throws JwtParsingException      if the token cannot be parsed or contains insufficient data
 */
PasswordlessUser decodeTokenToUser(String token) throws ...;
```

Write the happy path **and** one test per documented exception (expired, invalid, unparseable). Same idea for IAM (authenticated success + unauthenticated + unauthorized) and for data access (record found + not found + backing store unavailable).

### 3. Given / when / then — one cycle per test

Structure every test as a single **Arrange → Act → Assert** pass, marked with the Axelix comment convention (note the trailing periods):

```java
@Test
void decodesUserFromValidToken() {
    // given.
    String token = validTokenFor(user);

    // when.
    PasswordlessUser decoded = jwtDecoderService.decodeTokenToUser(token);

    // then.
    assertThat(decoded.getUsername()).isEqualTo(user.getUsername());
}
```

A `when → then → when → then` sequence (multiple act/assert cycles) means you have **more than one scenario** — split it into separate tests. Each test verifies exactly one thing.

### 4. Assert exception **type**, never message text

The exception type is part of the contract; its human-readable message is not. Assert the type only:

```java
// then.
assertThatThrownBy(() -> jwtDecoderService.decodeTokenToUser(expiredToken))
        .isInstanceOf(ExpiredJwtTokenException.class);
```

Do **not** use `hasMessage`, `hasMessageContaining`, message snapshots, or `expectErrorMessage`. The one exception: a stable, machine-readable `errorCode` that the contract explicitly documents may be asserted — descriptive prose never.

### 5. Parameterize repetitive cases

When the same behavior holds across a set of inputs, use a parameterized test instead of copy-pasted methods. Classic candidate: a method that returns `null`/throws for empty, `null`, and blank strings.

```java
@ParameterizedTest // GH-1234
@ValueSource(strings = {"", "   "})
@NullSource
void returnsEmptyForBlankInput(String input) {
    // given / when.
    Optional<Authority> result = resolver.resolve(input, HttpMethod.GET);

    // then.
    assertThat(result).isEmpty();
}
```

Each parameterized invocation is still a single given/when/then scenario. Reference the driving issue with a `// GH-NNNN` comment when there is one, as the codebase does.

### 6. Isolation — every test cleans up after itself

Test A must never depend on data or state left by test B, and order must not matter. Whatever a test mutates, it must reset:

- **Database rows / files / caches:** use transactional rollback, `@AfterEach` cleanup, or per-test containers. Do not reach for `@DirtiesContext` as a crutch.
- **Shared/static state and Spring context:** reset in `@BeforeEach`/`@AfterEach`; prefer fresh mocks per test.
- **No** `@Order`, no implicit reliance on execution sequence, no reusing an ID/token another test created.

### 7. Skip nullability noise

Do not add assertions that merely check a value is non-null when the real assertion already implies it, or when null was never plausible. They add noise and verify almost nothing. Assert the meaningful property instead (`assertThat(user.getUsername()).isEqualTo(...)` already proves `user` is non-null).

### 8. Group related tests with `@Nested`

When a unit has several distinct scenario groups (e.g. "enabled" vs "disabled", "found" vs "not found"), group them in `@Nested` static inner classes with descriptive names, as the codebase does:

```java
class ActuatorPrometheusEndpointTest {

    @Nested
    class WhenEnabled { /* tests */ }

    @Nested
    class WhenDisabled { /* tests */ }
}
```

Do not introduce a single `@Nested` class holding only one category — nesting earns its keep only when it separates groups.

## Project conventions (Axelix)

- **Assertions:** AssertJ (`import static org.assertj.core.api.Assertions.assertThat;`) and `assertThatThrownBy`. Match whatever the neighboring tests use.
- **Comments:** `// given.` / `// when.` / `// then.` with trailing periods.
- **Naming:** either plain intent (`decodesUserFromValidToken`) or the `shouldX_whenY` form — follow the file you're adding to.
- **Types:** prefer explicit types over `var` when the type isn't obvious from the right-hand side.
- **Copyright header:** copy the license header block from a sibling test file into any new test file.

## Workflow to follow

1. Read the source's public API and Javadoc/TSDoc → build the contract checklist (§ "Before writing").
2. Read one or two neighboring test files to lock onto local style, imports, and helpers.
3. Draft tests: one per checklist item, positive and negative, parameterizing repetitive inputs, grouping with `@Nested` when there are multiple scenario groups.
4. Apply the eight standards to each test as you write it.
5. **Run the tests** (via the module's Gradle/npm task) and confirm they pass. Fix failures — a test you didn't run is not done.
6. Self-check against the list below before handing off.

## Self-check before finishing

```
- [ ] Every documented behavior (success + each exception/edge) has a test
- [ ] Only public/exported API is exercised; no reflection into internals
- [ ] Each test is a single given → when → then; no act/assert/act/assert
- [ ] Exceptions asserted by type only; no message-text assertions
- [ ] Repetitive input sets are parameterized, not copy-pasted
- [ ] Each test cleans up its own DB/shared/context state; order-independent
- [ ] No noise-only non-null assertions
- [ ] Related scenario groups organized with @Nested
- [ ] Tests actually run and pass
```
