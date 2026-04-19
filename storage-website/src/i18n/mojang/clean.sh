#!/usr/bin/env bash

set -euo pipefail

file="$1"

tmp="$(mktemp)"

jq 'with_entries(select(.key | startswith("item.minecraft.") or startswith("block.minecraft.")))' "$file" > "$tmp"

mv "$tmp" "$file"
