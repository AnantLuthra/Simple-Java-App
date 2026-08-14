# Java Maven App - Base Jenkins Pipeline

This branch is the baseline Jenkins pipeline for the Java Maven app.

## What it does

- Builds the application with Maven.
- Builds a Docker image.
- Deploys through the pipeline flow defined in `Jenkinsfile`.
- Uses local Groovy helpers from `script.groovy`.

## Pipeline flow

1. `init`
   - Loads `script.groovy`.
   - Initializes the helper script.
2. `build jar`
   - Runs `gv.buildJar()`.
   - Packages the app with Maven.
3. `build image`
   - Runs `gv.buildImage()`.
   - Builds the Docker image.
   - Uses Jenkins credentials for Docker Hub access.
4. `deploy`
   - Runs `gv.deployApp()`.
   - Handles the deploy step from the helper script.

## Repository layout

- `Jenkinsfile` - declarative Jenkins pipeline.
- `script.groovy` - local pipeline helper functions.
- `src/main` - Spring Boot application source.
- `src/test` - application tests.
- `Dockerfile` - container build definition.

## Prerequisites

- Jenkins with the Maven tool configured as `maven-3.9`.
- Docker access on the Jenkins agent.
- Jenkins credentials with id `docker-hub-repo`.

## Notes

- This branch is the clean base version of the pipeline.
- It keeps the build, image, and deploy logic in one place without using a shared library.
