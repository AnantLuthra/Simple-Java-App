# Java Maven App - Jenkins Job Playground

This branch is the experimental Jenkins job version of the Java Maven app.

## What it does

- Explores Jenkins pipeline features like parameters and manual input steps.
- Keeps the application flow around build, test, image creation, and deploy.
- Uses `sample.groovy` for helper logic instead of the base script.
- Includes a small shell sample in `sample-script.sh`.

## Pipeline flow

1. `init`
   - Loads `sample.groovy`.
   - Calls the `init()` helper.
2. `testing`
   - Runs only when `executeTests` is enabled.
   - Calls the `testing()` helper.
3. `build jar`
   - Kept as a pipeline stage for the build flow.
4. `build image`
   - Prompts for a health check input before continuing.
   - Calls `build_image()` from the helper script.
5. `deploy`
   - Uses a manual input gate.
   - Lets the job decide whether to deploy and whether to keep artifacts.

## Jenkins job features

- `NAME` parameter for a custom run name.
- `Build type` choice parameter for build variation.
- `executeTests` boolean parameter to control the test stage.
- Manual `input` steps for controlled progress through the pipeline.

## Repository layout

- `Jenkinsfile` - declarative Jenkins pipeline.
- `sample.groovy` - helper functions for this exploratory branch.
- `sample-script.sh` - simple shell sample.
- `script.groovy` - base helper script kept in the branch for comparison.
- `src/main` - Spring Boot application source.
- `src/test` - application tests.

## Prerequisites

- Jenkins with the Maven tool configured as `maven-3.9`.
- Docker access on the Jenkins agent.
- Jenkins credentials available for the image build flow.

## Notes

- This branch is meant for testing Jenkins behavior and pipeline interactions.
- The job is designed to be run as a separate branch target in Jenkins.
