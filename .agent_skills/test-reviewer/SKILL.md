---
name: test-reviewer
description: Reviews test code in GitHub pull requests for isolation, public-API contract coverage, AAA structure, and correct exception assertions. Use whenever reviewing a PR that adds, modifies, or deletes tests (Java, Kotlin, TypeScript, JavaScript), when the user asks to review tests or test quality, when evaluating whether test changes are merge-ready, or when commenting on PR test coverage — even if the user only says "review this PR" and the diff includes test files.
---

# Test Reviewer

Guide for AI agents reviewing **test changes** in GitHub pull requests for the Axelix monorepo (`master/`, `sbs/`, `common/`, `front-end/`).

Apply this skill whenever a PR **creates, modifies, or deletes** test files — including when test changes are a small part of a larger PR.

## When to apply

- PR diff touches `*Test.java`, `*Test.kt`, `*.test.ts`, `*.test.tsx`, `*.spec.ts`, Cypress specs, or similar.
- User asks to review tests, test quality, or whether tests follow project standards.
- User asks whether a PR is merge-ready and tests are in scope.

If the PR has **no** test changes, do not apply this skill unless the user explicitly asks you to evaluate missing test coverage for new production code.

## Review workflow

1. **Identify scope** — List added/changed test files and the production code they claim to cover (`git diff` / PR files API).
2. **Find the contract** — For each tested unit, locate the public API:
   - **Java/Kotlin:** `public` methods on interfaces or classes; prefer interface Javadoc as the contract source.
   - **TypeScript/JavaScript:** exported functions, classes, hooks, or components from the module under test.
3. **Map contract → cases** — From Javadoc/TSDoc and declared behavior, list required positive and negative test cases before reading assertions.
4. **Review each test method** against the five standards below.
5. **Report** using the output format at the end. **Block merge** on any 🔴 finding.

## Standards

### 1. Test isolation (merge-blocking)

Tests must be isolated. Test A must not in any way possible depend on the data/outcome of test B. Every test must fully clean up after itself the changes it potentially made:

- to the database
- to the shared state in the context and so on

If the test does not clean that up — that is a bug and it must be addressed. We cannot merge such PR.

**Look for:**

- Shared mutable static fields, singletons, or Spring test context pollution without reset.
- Database rows, files, or caches left behind (missing `@Transactional` rollback, `@DirtiesContext` used as a crutch, no `afterEach`/`@AfterEach` cleanup).
- Order-dependent tests (`@Order`, implicit reliance on execution sequence).
- Reuse of IDs or tokens created by another test without setup in the same test.

**Acceptable patterns:** `@BeforeEach` / `@AfterEach` (or JUnit 5 equivalents) that reset state; transactional tests that roll back; fresh mocks per test; dedicated test containers with per-test schema/data setup.

### 2. Test the public API only

The public API is the API that is:

- in Java, it is marked with `public` keyword and it ideally present on the interface and has a javadoc
- in javascript/typescript, it is a function that is exported from the module

Do not test the private/internal methods — these are the details of implementation. Good test just tests the contract. The contract is:

- What the API accepts
- What the API returns in what cases
- What exceptions/errors does it throws and in what cases
- What concurrency guarantees it has and in what cases

And so on.

**Example — interface contract:**

```java
public interface AuthorityResolver {

    /**
     * Resolves the required {@link Authority} for the given request relative path.
     *
     * @param relativeRequestPath  the relative request path with prefix already split. E.g. {@code /axelix-beans}
     *                             is correct, {@code /actuator/axelix-beans} is not, {@code /beans/feed} is correct,
     *                             {@code /api/external/beans/feed} is not.
     * @param httpMethod           the HTTP method (e.g. {@link HttpMethod#GET}).
     *
     * @return                     an {@link Optional} containing the required {@link Authority},
     *                             or {@link Optional#empty()} if no authority is associated with the relative request path
     */
    Optional<Authority> resolve(String relativeRequestPath, HttpMethod httpMethod);
}
```

```java
public class CachingAuthorityResolver implements AuthorityResolver {

    private final AuthorityResolver delegate;

    /**
     * Resolves the required {@link Authority} for the given request relative path, possibly from cache
     * if already resolved. If not - we just hit the delegate.
     */
    public Optional<Authority> resolve(String relativeRequestPath, HttpMethod httpMethod) {
        // implementation
    }
}
```

What we should test is:

- the contract outlined in the javadoc of the interface
- and the capabilities that this implementation also declares. In this particular case, it declares that it may take data from cache. In this particular case it may be done by inserting the Mock into the delegate and then counting invocations on the mock.

**Flag as 🔴 when:**

- Tests call package-private/private methods via reflection or `@VisibleForTesting` solely to assert internals.
- Tests duplicate production logic instead of asserting observable outcomes.
- Implementation-only behavior is tested but the interface contract is not.

**Flag as 🟡 when:**

- Public API is tested but implementation-specific guarantees (e.g. caching) declared in the class Javadoc are untested.

### 3. AAA structure — given, when, then

Tests should have clear stages: given, when, then. Patterns like

- when, then, when, then

or any other duplications or permutations are bad. The golden rule is AAA — Arrange, Act, Assert. We never do things like Act, Assert, Act, Assert and so on. If we have to do that — this must be a separate test case. It must not be a single test case.

**Required:** One `// given.`, one `// when.`, one `// then.` per test method (Axelix convention from AGENTS.md).

**Flag as 🔴 when:** Multiple act/assert cycles in one test; missing stage comments; several scenarios packed into one `@Test`.

**Acceptable:** Parameterized tests (`@ParameterizedTest`) where each invocation is a single AAA scenario.

### 4. Positive and negative coverage

Tests must be present for all cases — both positive and negative. Most of the time both negative and positive cases can be derived from the contract (like by reading javadoc).

**Example — derive cases from Javadoc:**

```java
public interface JwtDecoderService {

    /**
     * Parses the given JWT token and converts it into a {@link User}.
     *
     * @param token the JWT token to decode
     * @return the reconstructed {@link User}
     * @throws ExpiredJwtTokenException if the JWT token has expired
     * @throws InvalidJwtTokenException if the JWT token is invalid or tampered with
     * @throws JwtParsingException if the token cannot be parsed or contains insufficient data
     */
    PasswordlessUser decodeTokenToUser(String token)
            throws ExpiredJwtTokenException, InvalidJwtTokenException, JwtParsingException;
}
```

We should not only test the successful user decoding from the token, but we should test the cases when we got the expired JWT, the JWS is not valid, or JWT cannot be parsed at all. These are negative cases and they absolutely must be present along with the "happy path".

**Workflow:** Build a contract checklist (inputs, success outputs, each documented exception, boundary values). Mark each item covered / missing.

**Flag as 🔴 when:** Documented failure modes or edge cases have no test.

**Flag as 🟡 when:** Coverage is plausible but contract checklist was not obvious from test names — suggest renaming or a missing-case comment in the PR.

### 5. Exception type only — not message text

The exception's error textual message is never a part of the contract. Some "errorCode" very well might be, but textual descriptive message — never. Thus, it should never be verified in tests as it is not the part of the contract.

For example, in the JwtDecoderService's case, checking for the exception's message in the test, whether this message contains something is bad. The message of the exception is not the part of the exception's contract. The nominal type IS the part of the exception's contract, but the message is not.

**Assert:** `assertThrows(ExpiredJwtTokenException.class, ...)` or `assertThatThrownBy(...).isInstanceOf(...)`.

**Do not assert:** `hasMessage`, `hasMessageContaining`, `expectErrorMessage`, snapshot of exception text, unless the contract explicitly documents a stable machine-readable code (not prose).

**Flag as 🔴 when:** Tests assert exception message strings or human-readable descriptions.

## Severity

| Level | Meaning | Merge |
|-------|---------|-------|
| 🔴 **Blocking** | Violates isolation, tests internals, broken AAA, missing contract cases, or asserts exception messages | **Do not approve** until fixed |
| 🟡 **Suggestion** | Style, naming, optional implementation-guarantee coverage, clearer arrange setup | Approve with comments optional |
| 🟢 **Note** | Minor polish, unrelated to standards | Informational |

Any 🔴 finding means: **we cannot merge such PR** (for test-related defects).

## Deriving the contract checklist

For each public entry point under test:

1. Read interface Javadoc / exported function TSDoc.
2. List parameters and valid/invalid domains.
3. List return shapes for success paths.
4. List each declared exception or error type and its documented trigger.
5. List implementation-added guarantees (cache, idempotency, concurrency) from class-level docs.
6. Compare checklist to test methods — report gaps.

For REST or integration tests, the contract is the HTTP API (status codes, response body shape, auth behavior) — same rules apply.

## Project conventions (Axelix)

- Prefer `@Nested` inner classes when multiple groups of related tests exist for the same API; avoid a single `@Nested` for only one category.
- Test file naming: `<SourceName>Test` in the same Gradle/npm module as the source.
- Use explicit types instead of `var` when the type is not obvious from the right-hand side (Java style guide in AGENTS.md).

## Review output format

Use this structure in PR review comments or summary:

```markdown
## Test review

**Scope:** [files / APIs reviewed]

### Contract coverage
| Case | Status |
|------|--------|
| [happy path / exception / edge] | ✅ covered in `FileTest.method` / ❌ missing |

### Findings

#### 🔴 Blocking
- **[file:line]** — [isolation | internal API | AAA | missing case | exception message] — [what's wrong] — [what to do]

#### 🟡 Suggestions
- **[file:line]** — [brief suggestion]

### Verdict
**[Approve tests / Request changes — N blocking issue(s)]**
```

For inline GitHub comments, one finding per comment; cite the test method and link the production contract (interface Javadoc or export).

## Quick checklist

Copy and complete when reviewing:

```
- [ ] All changed tests are isolated (DB + shared state cleaned per test)
- [ ] Only public/exported API is exercised; no private/internal testing
- [ ] Each test: single given → when → then (one act, one assert phase)
- [ ] Positive + negative cases derived from contract are present
- [ ] Exceptions asserted by type only (no message text assertions)
- [ ] Implementation-declared guarantees (e.g. caching) covered when applicable
```
