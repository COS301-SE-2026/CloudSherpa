#!/bin/bash

# This script enumerates all directories recursively starting from the root directory and copies the
# .env.schema files to .env files. It is idempotent, meaning you can run it as many times as needed,
# it will not overwrite existing .env files.

# USAGE (script will only work in linux-based environments, ensure the script has read/write permissions)
# sudo chmod +x env-init.sh
# ./env-init.sh

find "../" -type f -name ".env.schema" | while read -r filepath; do
    dir="$(dirname "$filepath")"
    base=".env"
    dest="$dir/$base"

    # .env does not exist
    if [[ ! -e "$dest" ]]; then
        # copy .env.schema to .env
        cp "$filepath" "$dest"
        echo "Copied: $filepath -> $dest"
        # remove varlock commets (first non whitespace char is # indicating comment)
        sed -i '/^[[:space:]]*#/d' "$dest"
    else
        echo ""$dest" already exists, SKIPPING"
    fi
done