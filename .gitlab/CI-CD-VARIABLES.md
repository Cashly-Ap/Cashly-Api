# GitLab CI/CD Variables Configuration Guide

This document describes the required environment variables for the GitLab CI/CD pipeline.

## Required GitLab Variables

Configure these variables in GitLab Project Settings > CI/CD > Variables:

### Docker Registry Variables (Automatically provided by GitLab)

- `CI_REGISTRY`: GitLab Container Registry URL
- `CI_REGISTRY_USER`: Registry username  
- `CI_REGISTRY_PASSWORD`: Registry password
- `CI_REGISTRY_IMAGE`: Full image name

### Environment Variables for Deployment

#### Staging Environment
- `STAGING_DB_URL`: Database URL for staging environment
- `STAGING_DB_USER`: Database username for staging
- `STAGING_DB_PASSWORD`: Database password for staging (mark as protected and masked)
- `STAGING_JWT_SECRET`: JWT secret key for staging (mark as protected and masked)

#### Production Environment
- `PROD_DB_URL`: Database URL for production environment
- `PROD_DB_USER`: Database username for production
- `PROD_DB_PASSWORD`: Database password for production (mark as protected and masked)
- `PROD_JWT_SECRET`: JWT secret key for production (mark as protected and masked)

## Pipeline Stages

1. **Test**: Uses `Dockerfile.test` to run all tests in containerized environment
2. **Build**: Validates compilation 
3. **Package**: Creates JAR and builds production Docker image using `Dockerfile`
4. **Deploy**: Manual deployment to staging/production environments

## Security Best Practices

1. **Mark sensitive variables as:**
   - Protected: Only available to protected branches/tags
   - Masked: Hidden in job logs

2. **Sensitive variables include:**
   - All passwords
   - JWT secrets
   - API keys
   - Database credentials

## How to Set Variables in GitLab

1. Go to your project in GitLab
2. Navigate to Settings > CI/CD
3. Expand the "Variables" section
4. Click "Add variable"
5. Enter the key and value
6. Check "Protected" for production variables
7. Check "Masked" for sensitive values
8. Click "Add variable"

## Testing the Pipeline

1. Push changes to a feature branch to trigger merge request pipeline
2. Merge to `develop` branch to trigger staging deployment
3. Merge to `main` branch to trigger production-ready pipeline
4. Create a tag to trigger production deployment