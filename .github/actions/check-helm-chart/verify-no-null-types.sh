#!/usr/bin/env bash
set -euo pipefail

schema="${1:?schema file path is required}"

null_paths=$(jq -r '
  [paths as $p |
    (getpath($p) | select(type == "object" and has("type"))) as $obj |
    if $obj then
      ($obj.type | if type == "array" then . else [.] end) as $types |
      if ($types | index("null")) then
        ($p | map(if type == "number" then "[\(.)]" else . end) | join("."))
      else empty end
    else empty end
  ] | unique | .[]
' "$schema")

if [ -n "$null_paths" ]; then
  echo "::error::${schema} contains properties with type \"null\":"
  echo "$null_paths"
  exit 1
fi

echo "No null types found in ${schema}"
