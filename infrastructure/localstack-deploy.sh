#!/bin/bash
set -e

# This sets all the magic variables
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
export AWS_ENDPOINT_URL=http://localhost:4566

# This creates the bucket and ignores errors if it already exists
echo "Ensuring S3 bucket 'localstack-cf-templates' exists..."
aws --endpoint-url=http://localhost:4566 s3 mb s3://localstack-cf-templates || true

# This is your deploy command
echo "Deploying CloudFormation stack..."
aws --endpoint-url=http://localhost:4566 cloudformation deploy \
    --stack-name patient-management \
    --template-file "./cdk.out/localstack.template.json" \
    --s3-bucket localstack-cf-templates \
    --s3-prefix patient-management-stack \
    --capabilities CAPABILITY_IAM

echo "Deployment successful!"
