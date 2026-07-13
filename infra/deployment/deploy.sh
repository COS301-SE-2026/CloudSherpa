#!/bin/bash

if [[ -z "${1:-}" ]]; then
    # Perhaps validate if valid SHA hash
    echo "Commit SHA commandline argument required"
    exit 1
fi

export IMAGE_TAG="$1" 

# Dynamically set AWS_ACCOUNT_ID, assumes instance profile is correctly configured
export AWS_ACCOUNT_ID="$(aws sts get-caller-identity --query 'Account' --output text)"

# Set AWS_REGION, maybe later fetch dynamically
export AWS_REGION="eu-north-1"

export AWS_ECR_REGISTRY="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$AWS_ECR_REGISTRY"

# Paths hardcoded like this for now
cd /opt/CloudSherpa
docker compose down
docker compose pull
docker compose up -d