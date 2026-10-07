# Java Maven App - Jenkins CI/CD Pipeline for AWS EKS

Complete CI/CD pipeline for the Java Maven application: versioning, build, Docker image publishing, and deployment to Amazon EKS.

## Technologies used

- AWS EKS, Jenkins, Docker, Kubernetes, Linux, Git, Java, Maven, Docker Hub

## Project description

This project implements a complete CI/CD flow for the Java Maven application. Jenkins increments the Maven version, builds the JAR, builds and pushes a versioned Docker image to Docker Hub, and deploys that image to an AWS EKS cluster using the Kubernetes manifests in the `kubernetes/` folder.

## Pipeline flow

1. `init`
   - Prints pipeline startup messages.
2. `increment version`
   - Uses `build-helper:parse-version` and `versions:set` to increment the Maven version.
   - Reads the updated version from `pom.xml` and sets `IMAGE_NAME` to `<version>-<Jenkins build number>`.
3. `build jar`
   - Runs `mvn clean package`.
4. `build image`
   - Builds `anantluthra/simple-java-app:${IMAGE_NAME}`.
   - Logs in to Docker Hub with Jenkins credentials and pushes the image.
5. `deploy`
   - Supplies AWS credentials from Jenkins and targets the `ap-south-1` region.
   - Uses `envsubst` to substitute `APP_NAME` and `IMAGE_NAME` into `kubernetes/deployment.yaml` and applies the resulting Deployment to AWS EKS with `kubectl`.
   - Substitutes `APP_NAME` into `kubernetes/service.yaml` and applies the Service to AWS EKS.
   - The Deployment pulls the just-published Docker Hub image and runs two replicas of the application. The Service exposes port `80` and forwards traffic to the container's port `8080`.
6. `commit version update`
   - Commits the Maven version change and pushes it back to the `ci-cd-eks` branch.

## Jenkins prerequisites

The Jenkins controller or agent that executes this pipeline needs:

- Maven configured in Jenkins as `maven-3.9`.
- Docker installed and usable by the Jenkins user.
- `kubectl` installed and available on the job's `PATH` so it can apply the EKS manifests.
- `envsubst` installed and available on the job's `PATH`. It is provided by the `gettext-base` package on Debian/Ubuntu systems and is used to replace `$APP_NAME` and `$IMAGE_NAME` in the manifest templates before `kubectl apply` runs.
- A kubeconfig file at `<jenkins-user-home>/.kube/config` inside the Jenkins container (for the standard Jenkins container user, this is usually `/var/jenkins_home/.kube/config`). `kubectl` uses this file to locate and authenticate to the EKS cluster.
- `aws-iam-authenticator` installed inside the Jenkins container at `/usr/bin/aws-iam-authenticator`. The kubeconfig's `exec` configuration invokes this binary to obtain an AWS IAM authentication token whenever `kubectl` connects to EKS.

Create these credentials in Jenkins, using the exact IDs referenced by the Jenkinsfile:

- `docker-hub-repo` - Docker Hub username/password credential used to log in and push the image.
- `jenkins-aws_access_key_id` - AWS access-key credential used during EKS deployment.
- `jenkins-aws_secret_access_key` - AWS secret-access-key credential used during EKS deployment.
- `git-credentials` - GitHub username/password or token credential used to push the version-bump commit.

The AWS IAM identity represented by the two AWS credentials must be authorized to access the EKS cluster and create/update the Kubernetes resources used by this pipeline.

## EKS kubeconfig and AWS IAM authentication

The `config` file must be present inside the Jenkins container at `<jenkins-user-home>/.kube/config`; in a standard Jenkins container, use `/var/jenkins_home/.kube/config`. This pipeline relies on a kubeconfig with an `exec` authentication section like the following. Replace `<endpoint-url>` and `<cluster-name>` with the values for your EKS cluster:

```yaml
apiVersion: v1
kind: Config
clusters:
- cluster:
    certificate-authority-data: /etc/kubernetes/pki/ca.crt
    server: <endpoint-url>
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    user: aws
  name: aws
current-context: aws
users:
- name: aws
  user:
    exec:
      apiVersion: client.authentication.k8s.io/v1beta1
      command: /usr/bin/aws-iam-authenticator
      args:
        - "token"
        - "-i"
        - "<cluster-name>"
```

When the deploy stage runs `kubectl apply`, `kubectl` reads this kubeconfig and executes `/usr/bin/aws-iam-authenticator token -i <cluster-name>`. The authenticator uses the AWS credentials injected by Jenkins to generate an EKS authentication token. This is why both the kubeconfig file and the `aws-iam-authenticator` binary must exist inside the Jenkins container, and why the binary path in the config must match its installed location.

## Kubernetes manifests and Docker Hub secret

The deploy stage applies these templates:

- `kubernetes/deployment.yaml` - defines the application Deployment, two replicas, and the image `anantluthra/simple-java-app:$IMAGE_NAME`.
- `kubernetes/service.yaml` - defines the Service for `$APP_NAME` on port `80`, targeting container port `8080`.

`deployment.yaml` contains the following image pull secret reference:

```yaml
imagePullSecrets:
  - name: my-registry-key
```

Before running the pipeline, create a Docker Hub registry secret named `my-registry-key` in the target EKS namespace. It must contain Docker Hub credentials that can pull `anantluthra/simple-java-app`. For the default namespace, for example:

```sh
kubectl create secret docker-registry my-registry-key \
  --docker-server=https://index.docker.io/v1/ \
  --docker-username=<dockerhub-username> \
  --docker-password=<dockerhub-password-or-token> \
  --docker-email=<email>
```

If you deploy into another namespace, create the same secret in that namespace or update the manifest to reference the correct secret name. Keep the registry secret and Jenkins credentials out of source control.

## Repository layout

- `Jenkinsfile` - declarative Jenkins CI/CD pipeline.
- `kubernetes/deployment.yaml` - EKS Deployment template.
- `kubernetes/service.yaml` - EKS Service template.
- `src/main` - Spring Boot application source.
- `src/test` - application tests.
- `Dockerfile` - application container image definition.
- `pom.xml` - Maven project definition.
