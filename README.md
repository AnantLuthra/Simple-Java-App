# Java Maven App - Jenkins Pipeline

This branch has the full Jenkins pipeline for the Java Maven app.

## What it does

- Loads local Groovy helpers from `script.groovy`.
- Increments the Maven version before the build.
- Builds the application with Maven.
- Builds and pushes the Docker image to Docker Hub.
- Runs the deploy step from the helper script.
- Commits the version update back to `master`.

## Pipeline flow

1. `init`
   - Loads `script.groovy`.
   - Prints the startup messages.
2. `increment version`
   - Runs the Maven version bump using `build-helper:parse-version` and `versions:set`.
   - Reads the updated version from `pom.xml`.
   - Sets `IMAGE_NAME` to `<version>-<build number>`.
3. `build jar`
   - Runs `gv.buildJar()`.
   - Executes `mvn clean package`.
4. `build image`
   - Runs `gv.buildImage()`.
   - Builds the Docker image as `anantluthra/simple-java-app:${IMAGE_NAME}`.
   - Logs in to Docker Hub with Jenkins credentials.
   - Pushes the image to Docker Hub.
5. `deploy`
   - Runs `gv.deployApp()`.
   - Handles the deploy step from the helper script.
6. `commit version update`
   - Configures the Git user in the Jenkins agent.
   - Commits the version bump.
   - Pushes the change back to `master`.

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
- Jenkins credentials with id `git-credentials`.

## Notes

- `Dockerfile` uses `amazoncorretto:17-alpine-jdk`, exposes port `8080`, and runs the packaged jar from `/usr/app`.
- The image tag is based on the application version plus the Jenkins build number.
- The pipeline keeps the build, image, deploy, and version update logic in one place without using a shared library.
- Other related branches in this repo are `jenkins-job` for the exploratory Jenkins job flow and `jen-shared-lib` for the shared-library based pipeline.
