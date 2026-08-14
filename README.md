# Java Maven App - Jenkins Shared Library Branch

This branch shows the application pipeline wired to a Jenkins Shared Library.

## What it does

- Builds the Java app with Maven.
- Uses a shared Jenkins library for reusable pipeline steps.
- Builds and pushes a Docker image.
- Keeps deployment logic in the main pipeline while reusing common functions from the shared library.

## Pipeline flow

1. `init`
   - Loads `script.groovy`.
   - Prepares the pipeline context.
2. `build jar`
   - Calls `buildJar()` from the shared library.
3. `build & push image`
   - Calls `buildImage(imageName)`.
   - Authenticates with Docker Hub through `dockerLogin()`.
   - Pushes the image with `dockerPush(imageName)`.
4. `deploy`
   - Calls the local `deployApp()` helper.

## Shared library setup

This branch expects a Jenkins shared library named `jenkins-shared-library`.

Shared library repo: `https://github.com/AnantLuthra/jenkins-shared-library`

The library provides reusable steps from:

- `vars/buildJar.groovy`
- `vars/buildImage.groovy`
- `vars/dockerLogin.groovy`
- `vars/dockerPush.groovy`
- `src/com/example/Docker.groovy`

In Jenkins, this library is typically configured under Global Pipeline Libraries and linked to a GitHub repository.

## Repository layout

- `Jenkinsfile` - declarative Jenkins pipeline.
- `script.groovy` - local helper for deployment.
- `src/main` - Spring Boot application source.
- `src/test` - application tests.
- `Dockerfile` - container build definition.

## Prerequisites

- Jenkins with the Maven tool configured as `maven-3.9`.
- A configured shared library named `jenkins-shared-library`.
- Docker access on the Jenkins agent.
- Jenkins credentials with id `docker-hub-repo`.

## Notes

- The image name used in the pipeline is `anantluthra/simple-java-app:2.0`.
- This branch is meant to keep the Jenkinsfile smaller by moving reusable Docker and build logic into the shared library.
