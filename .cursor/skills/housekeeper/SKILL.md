---
name: housekeeper
description: Perform housekeeping tasks to manage the complexity of the code base. Use this skill when asked to perform tasks related to maintenance of the repository, refactoring of any kind, management of the technical debt or writing/revisiting tests.
---

# Managing the technical debt

The skill provides the structured workflow for managing the technical debt in the project. Act as an active guide, walking users through the stages:

- Context Gathering
- Refinement & Structure, 
- Spec Generation 
- Execution

## When to offer this workflow

**Trigger conditions:**

- User mentions the refactoring of some part of the code
- User mentions changes in the structure of the files inside the project
- User mentions any organizational change 

## Overall Principles

When applying refactoring or making an organizational changes to the code base in overall, **follow this strict rules**:

1. If unsure - ask. 
2. Do only what has been asked, do not refactor/enahnce/etc anything else unless asked otherwise.
3. The general rule of thumb is the less code - the better. Less code generally means less bugs. 
4. Refactoring must **NEVER** be massive. It involves large risks of breaking existing code, and it is difficult to review. If you can, then make as small and as atomic changes as possible, one at a time.
5. Duplication of code is generally bad. Extracting common parts is generally a good thing to be doing.
6. If you move source files around, please, preserve the author javadoc/tsdoc tags

## Refactoring Tests Code

When applying refactoring or making an organizational changes to the tests code base, **follow this strict rules**:

1. For any given source file A, the dedicated test file for this class must have the name of the A + "Test" postfix, and this test file must reside inside the same Gradle module as the source file.
2. Tests must cover all the cases declared in the public API.
3. Ideally, each source file with logic in (not counting DTO/POJO/Java records and similar) should be covered with test cases. But there is a caveat. Sometimes this leads to almost identical Tests and thus the duplication of code, which is bad. So this rule should be handled with care. 
4. Integration tests should be preferred over the unit tests.