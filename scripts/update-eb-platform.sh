#!/bin/bash
set -e

CURRENT_PLATFORM=$(aws elasticbeanstalk describe-environments \
  --environment-names "$EB_ENVIRONMENT_NAME" \
  --region "$AWS_DEFAULT_REGION" \
  --query "Environments[0].PlatformArn" \
  --output text)

echo "Current platform: $CURRENT_PLATFORM"

if echo "$CURRENT_PLATFORM" | grep -qi "docker"; then
  echo "Platform is already Docker, skipping update."
else
  echo "Updating platform to Docker..."
  aws elasticbeanstalk update-environment \
    --environment-name "$EB_ENVIRONMENT_NAME" \
    --solution-stack-name "64bit Amazon Linux 2023 v4.11.0 running Docker" \
    --region "$AWS_DEFAULT_REGION"
  echo "Waiting for platform update to complete..."
  aws elasticbeanstalk wait environment-updated \
    --environment-name "$EB_ENVIRONMENT_NAME" \
    --region "$AWS_DEFAULT_REGION"
  echo "Platform updated successfully."
fi
