#!/bin/bash

# USAGE
# sudo chmod +x scripts/mkdocs-local.sh
# Run from root directory, ./scripts/mkdocs-local.sh

if [[ ! -d "docs/.venv" ]]; then
    echo "Creating Python docs venv..."
    python3 -m venv docs/.venv
fi

source docs/.venv/bin/activate
pip install mkdocs mkdocs-material pymdown-extensions mkdocs-glightbox

mkdocs serve

