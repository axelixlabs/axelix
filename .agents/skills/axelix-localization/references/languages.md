# Locale guidance

Existing project conventions and user choices take precedence over these starting defaults. Report a chosen language variant and formality in the task notes. The guidance below is provisional style guidance, not a user-approved terminology glossary.

## French

If no convention exists, use `fr-FR`, professional technical prose, and `vous` when addressing the reader. Use natural French sentence capitalization and typography without inserting non-breaking characters into executable code. Preserve established framework identifiers. Choose one rendering of recurring technical terms and use it across docs and UI; avoid switching between a translated term and an English borrowing without reason.

## German

If no convention exists, use `de-DE`, professional technical prose, and `Sie` when direct address is needed. Respect German noun capitalization and natural compounds; do not mechanically mirror English title capitalization. Check long navigation labels, buttons, and table headings in the rendered UI. Keep framework identifiers recognizable and distinguish technical terms by their actual meaning in context.

## Spanish

If no convention exists, use broadly understandable Spanish and neutral instructions, avoiding regional idioms. Record the actual runtime locale variant separately: do not silently equate generic `es` with `es-ES` or `es-MX`. If direct address is necessary and no project rule exists, use formal `usted` consistently. Use natural sentence capitalization and punctuation. Do not mix regional technical vocabulary within a feature.

## Russian and other languages

For Russian, inspect accepted `ru.json` strings and `.ru.mdx` pages before choosing terminology and formality. Existing translations are consistency evidence, not proof of correctness. For other languages, derive a small locale profile from project examples and explicit user preferences; state remaining assumptions. Apply the same semantic and structural review workflow to every language.

## Glossary record format

Use an existing glossary format where available. Otherwise record entries as:

| English term | Technical meaning | Locale | Target term | Allowed variants / retained identifiers | Status |
| --- | --- | --- | --- | --- | --- |

Distinguish `existing`, `proposed`, and `user-approved`. Populate entries from the task's actual product vocabulary. Never fabricate approval or present provisional language choices as native-reviewed.
