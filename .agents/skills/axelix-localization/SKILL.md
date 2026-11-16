---
name: axelix-localization
description: Translate, update, or review Axelix documentation and UI localization using English as canonical source, shared terminology, independent subagent reviews, back-translation, and structural checks. Use for French, German, Spanish, Russian, or other locale catalogs and translated MDX pages, including translation drift and cross-language consistency. Does not replace implementation work for unrelated UI changes or English-only documentation authoring.
---

# Axelix localization

Produce technically faithful, natural translations of Axelix docs and UI. English is canonical. Translate each language directly from English, never through another translation. AI review improves quality but does not establish native or professional verification.

## Scope and source contract

1. Identify requested pages or UI keys, target locales, and whether the task is translation, update, or review. Keep unrelated languages and source text unchanged unless requested.
2. Read repository instructions and existing locale conventions. Inspect:
   - Docs: `docs/content/docs/`, `docs/src/lib/constants.mjs`, `docs/src/lib/i18n.ts`, localized navigation metadata, and site UI translations.
   - Product UI: `master/front-end/src/i18n/locales/en.json`, target catalogs, `master/front-end/src/i18n/i18n.js`, calling components, and the language selector.
   These paths are starting points; verify the current implementation rather than assuming this list is exhaustive.
3. For docs, read [Fumadocs mechanics](../docs-writer/references/fumadocs.md). Apply [docs-writer](../docs-writer/SKILL.md) when authoring or correcting canonical product documentation. Its English/Russian pairing convention does not require creating unrelated Russian changes during a French-only translation request. Preserve English wording and structure during translation; do not import English editorial rules into target-language grammar.
4. Read the implementation where a label or claim is ambiguous. Build a source contract covering actions, actors, negation, prerequisites, permissions, quantities, units, defaults, versions, warnings, and causal or temporal conditions. Include relevant UI key-to-label mappings.
5. Record the English revision (commit plus dirty-source status, or content digest), file/key scope, glossary decisions, and locale conventions in working notes. Record the final reviewed target revision or digest as well. Keep these notes and intermediate review artifacts outside published content, normally in a temporary task directory. Use an existing translation tracking convention if present; do not invent a repository-wide tracking system.

If English is ambiguous or contradicts code, report the specific uncertainty. Continue unaffected work; do not invent a resolution in the target language. Correcting the source requires the same factual verification as authoring docs.

## Terminology and locale conventions

Read [language guidance](references/languages.md) for the requested locale. Locate existing project glossaries and accepted translations before proposing new terms.

Maintain one shared terminology record for docs and UI with: English term, technical meaning, target term, allowed variants, terms to retain in English, and status (`existing`, `proposed`, or `user-approved`). Do not claim inferred or AI-generated choices are user-approved. Store durable decisions with the skill's references when the task calls for maintaining terminology; otherwise report proposed decisions in the handoff.

Preserve Axelix, Spring Boot, Java identifiers, API names, configuration keys, authorities, paths, command flags, and executable examples. Translate explanatory prose and human-facing titles. Translate code comments or sample display text only when requested and without changing runnable behavior. Resolve technical senses such as Spring bean versus ordinary bean, JVM thread versus discussion thread, and service instance versus object instance.

Use exact target UI labels in docs. If no translated UI label exists, identify the mismatch and retain the actual visible label rather than inventing a button name. Do not treat an English screenshot as a target-language screenshot.

## Translation and independent review

Work in reviewable page or feature batches. The primary agent may translate directly or delegate translation. Reviewers must be separate from the translator and must not receive its rationale or previous verdicts.

### 1. Draft

Translate using the source contract, glossary, and locale conventions. Preserve meaning and information density; do not add product claims or remove caveats to improve fluency. Allow idiomatic sentence structure rather than literal English syntax.

### 2. Blind target-language review and back-translation

Launch a fresh subagent with no inherited conversation (`fork_turns: "none"` where supported). Give it only the target draft, target locale, glossary, intended audience, and minimal technical context. Do not give it canonical English, the source contract, or a glossary containing full source sentences. If the tool cannot isolate context, disclose that the review was not blind.

Ask it to:
- Read as a technical user and identify unnatural grammar, ambiguity, inconsistent terminology, tone, and unclear actions.
- Translate the target back into English faithfully, preserving ambiguity and mistakes rather than repairing the likely intent.
- Return a back-translation and concrete findings with file/section or UI key, target excerpt, explanation, severity, and suggested correction. Return no repository edits.

### 3. Bilingual semantic comparison

Launch a different fresh subagent with canonical English, the target draft, source contract, glossary, and locale conventions. Collect its direct source-to-target assessment first. Only then supply the blind back-translation in a follow-up and ask it to inspect discrepancies as additional evidence. This prevents the back-translation from anchoring the initial bilingual assessment.

Check omissions, additions, changed conditions, reversed actions, weakened warnings, wrong scope, permissions, defaults, numbers, units, and mismatched UI labels. A wording difference in the back-translation alone is not an error. A matching back-translation is not proof of correctness. Require findings tied to specific source and target passages; do not accept an unsupported quality score.

Example: English says restart is required only after a configuration change; target says restart is always required. Report the changed condition as a meaning defect, with both passages and a correction.

### 4. Resolve and recheck

Classify findings:
- **Blocking:** changed meaning, omitted instructions, broken identifiers/placeholders/markup, or contradictory UI labels.
- **Language:** grammar, ambiguity, terminology, or tone that impedes understanding.
- **Optional:** stylistic alternatives that preserve meaning and conventions.

Check each finding against the English source and implementation where relevant. Apply supported fixes, retaining intentional idiomatic differences. Review changed passages again with fresh reviewer context; include adjacent text for coherence. Previous findings may be supplied for a targeted recheck, but do not call that recheck blind.

Allow at most three revision/review rounds per batch. Stop earlier when no blocking or language findings remain and structural checks pass. Report unresolved disagreements and affected passages after the limit; do not declare them accepted or loop indefinitely. If subagents are unavailable, perform sequential passes and explicitly report the absence of independent review.

## Structural verification

Inspect project scripts and use the existing relevant commands. Do not install services or add dependencies merely to translate.

- **UI catalogs:** parse JSON; compare scoped keys and value types; preserve interpolation names, formatting tokens, nesting references, and rich-text tags. Check locale-specific i18next plural rules: target plural categories may differ from English, so do not demand identical plural suffixes. Check whole messages instead of translating concatenated fragments without context. Do not mistake English fallback behavior for completed translation.
- **Docs:** preserve MDX imports, component names and non-translatable props, code, command/configuration values, and table structure. Translate human-facing frontmatter. Validate links and heading anchors after localization; retain working destinations or update them using actual route conventions. Do not translate filesystem paths or URL slugs by guesswork. Run the relevant documentation build/checks.
- **Locale enablement, when requested:** inspect registration, switchers, fallback behavior, site navigation/search, date/number formatting, component-library locale, and cron descriptions for hardcoded language assumptions. Add only changes needed for the requested locale. Translating files alone does not establish runtime support.
- **Rendered inspection:** inspect changed pages/screens for layout, clipping, missing accents, long labels, rich text, and fallback strings when a preview is available. Record when visual verification cannot be performed. Do not claim a rendered check based on source inspection.

## Handoff

Report scope and locales, source/target revision or digest, terminology decisions, independent review passes actually performed, findings fixed or unresolved, structural/build results, and preview coverage. Keep this concise for small batches. Identify stale translations when the English source changed after review; recheck affected content before marking it current.

Use the status **AI translated and independently AI reviewed** only when both independent passes actually ran and their material findings were resolved. Otherwise identify the missing or incomplete checks. Claim human/native review only when it actually occurred. Do not publish or send translations to external services unless the user authorized that action.
