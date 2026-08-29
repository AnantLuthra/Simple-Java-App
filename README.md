# Java Maven App - Jenkins Pipeline

Complete the CI/CD Pipeline: Docker Compose and Dynamic Versioning

## Technologies used

- AWS, Jenkins, Docker, Linux, Git, Java, Maven, Docker Hub

## Project Description

This project implements a complete CI/CD flow for the Java Maven application. It covers dynamic versioning, artifact creation, Docker image publishing, and deployment through Docker Compose on EC2.

## What it does

- Loads local Groovy helpers from `script.groovy`.
- Increments the Maven version before the build.
- Builds the application with Maven.
- Builds and pushes the Docker image to Docker Hub.
- Deploys the new application version on EC2 using Docker Compose and `server-cmds.sh`.
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
   - Copies `docker-compose.yaml` and `server-cmds.sh` to the EC2 host.
   - Uses `server-cmds.sh` to export the target image and start the stack.
   - Starts the updated application container with Docker Compose.
   - Uses Docker Compose to manage the application container and the Postgres service.
6. `commit version update`
   - Configures the Git user in the Jenkins agent.
   - Commits the version bump.
   - Pushes the change back to `master`.

## Repository layout

- `Jenkinsfile` - declarative Jenkins pipeline.
- `script.groovy` - local pipeline helper functions.
- `server-cmds.sh` - remote deployment helper script.
- `docker-compose.yaml` - Docker Compose definition for the app and Postgres.
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
- The deploy step uses a shell script plus Docker Compose instead of a manual container run command.
- Other related branches in this repo are `jenkins-job` for the exploratory Jenkins job flow, `jen-shared-lib` for the shared-library based pipeline, and `cd-pipe` for the complete CI/CD flow with a project-specific shared library.
