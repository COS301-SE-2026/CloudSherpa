#!/bin/bash
set -o pipefail

if ! command -v varlock &> /dev/null; then
    echo "You do not have varlock installed"
    echo "Visit https://varlock.dev/ for installation instructions"
    exit 1
fi

# Execute script from anywhere within CloudSherpa repo
current_dir=$(pwd)

sherpa_found=0
subdir=0

IFS='/' read -r -a path_array <<< "$current_dir"

for dir in "${path_array[@]}"; do
    if [[ "$dir" == "CloudSherpa" ]]; then
        sherpa_found=1
    elif [[ $sherpa_found -eq 1 ]]; then
        subdir=$(( subdir + 1 )) 
    fi
done

if (( sherpa_found != 1 )); then 
    echo "You have moved this script outside of the CloudSherpa repository"
    exit 1
fi

while (( subdir > 0 )); do
    cd ..
    subdir=$(( subdir - 1 ))
done    

# Dashboard validation
echo "Validating DASHBOARD env vars"
(cd apps/dashboard && varlock load)

# Ingestion validation
echo "Validating INGESTION env vars"
(cd apps/ingestion && varlock load)

# Service validation
echo "Validating SERVICE env vars"
(cd apps/service && varlock load)
