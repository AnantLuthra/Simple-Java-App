# Java Maven App - Jenkins CI/CD Pipeline for Amazon ECR and AWS EKS

Complete CI/CD pipeline for the Java Maven application: versioning, build, publishing a Docker image to Amazon ECR, and deployment to Amazon EKS.

## Technologies used

- AWS ECR, AWS EKS, Jenkins, Docker, Kubernetes, Linux, Git, Java, Maven

## Project description

This project implements a complete CI/CD flow for the Java Maven application. Jenkins increments the Maven version, builds the JAR, builds and pushes a versioned Docker image to Amazon ECR, then deploys that exact image to AWS EKS using the Kubernetes manifests in the `kubernetes/` folder.

The image repository configured in the Jenkinsfile is:

```text
066949448876.dkr.ecr.ap-south-1.amazonaws.com/java-maven-app
```

## Pipeline flow

1. `init`
   - Prints pipeline startup messages.
2. `increment version`
   - Uses `build-helper:parse-version` and `versions:set` to increment the Maven version.
   - Reads the updated version from `pom.xml` and sets `IMAGE_NAME` to `<version>-<Jenkins build number>`.
3. `build jar`
   - Runs `mvn clean package`.
4. `build image`
   - Builds `${DOCKER_REPO}:${IMAGE_NAME}`.
   - Logs in to the configured Amazon ECR registry with Jenkins credentials and pushes the image.
5. `deploy`
   - Supplies AWS credentials from Jenkins and targets the `ap-south-1` region.
   - Uses `envsubst` to substitute `APP_NAME`, `IMAGE_NAME`, and `DOCKER_REPO` into `kubernetes/deployment.yaml`, then applies the resulting Deployment to AWS EKS with `kubectl`.
   - Substitutes `APP_NAME` into `kubernetes/service.yaml` and applies the Service to AWS EKS.
   - The Deployment pulls the just-published ECR image and runs two replicas. The Service exposes port `80` and forwards traffic to container port `8080`.
6. `commit version update`
   - Commits the Maven version change and pushes it back to the `ci-ecr-eks` branch.

## Jenkins prerequisites

The Jenkins controller or agent that executes this pipeline needs:

- Maven configured in Jenkins as `maven-3.9`.
- Docker installed and usable by the Jenkins user.
- `kubectl` installed and available on the job's `PATH` so it can apply the EKS manifests.
- `envsubst` installed and available on the job's `PATH`. It is provided by the `gettext-base` package on Debian/Ubuntu systems and substitutes the variables in the manifest templates before `kubectl apply` runs.
- A kubeconfig file at `<jenkins-user-home>/.kube/config` inside the Jenkins container. For the standard Jenkins container user, this is usually `/var/jenkins_home/.kube/config`.
- `aws-iam-authenticator` installed inside the Jenkins container at `/usr/bin/aws-iam-authenticator`.

Create these Jenkins credentials using the exact IDs referenced by the Jenkinsfile:

- `ecr-credentials` - username/password credential used for the Docker login and push to Amazon ECR.
- `jenkins-aws_access_key_id` - AWS access-key credential used during EKS deployment.
- `jenkins-aws_secret_access_key` - AWS secret-access-key credential used during EKS deployment.
- `git-credentials` - GitHub username/password or token credential used to push the version-bump commit.

The AWS IAM identities used by these credentials must have the required ECR push and EKS/Kubernetes deployment permissions.

## EKS kubeconfig and AWS IAM authentication

The `config` file must be present inside the Jenkins container at `<jenkins-user-home>/.kube/config`; in a standard Jenkins container, use `/var/jenkins_home/.kube/config`. This pipeline uses a kubeconfig with an `exec` authentication section such as the following. Replace `<endpoint-url>` and `<cluster-name>` with the values for your EKS cluster:

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

When the deploy stage runs `kubectl apply`, `kubectl` reads this kubeconfig and executes `/usr/bin/aws-iam-authenticator token -i <cluster-name>`. The authenticator uses the AWS credentials injected by Jenkins to generate an EKS authentication token. Therefore, both the kubeconfig file and `aws-iam-authenticator` must exist inside the Jenkins container, and the path in `command` must match the installed binary.

## Kubernetes manifests and Amazon ECR pull secret

The deploy stage applies these templates:

- `kubernetes/deployment.yaml` - defines the Deployment, two replicas, and the image `$DOCKER_REPO:$IMAGE_NAME`.
- `kubernetes/service.yaml` - defines the Service for `$APP_NAME` on port `80`, targeting container port `8080`.

`deployment.yaml` references the ECR image pull secret named `aws-registry-key`:

```yaml
imagePullSecrets:
  - name: aws-registry-key
```

Before running the pipeline, create `aws-registry-key` in the target EKS namespace with credentials that can pull from the ECR repository. For the default namespace, you can use the AWS CLI:

```sh
kubectl create secret docker-registry aws-registry-key \
  --docker-server=066949448876.dkr.ecr.ap-south-1.amazonaws.com \
  --docker-username=AWS \
  --docker-password="$(aws ecr get-login-password --region ap-south-1)" \
  --dry-run=client -o yaml | kubectl apply -f -
```

Amazon ECR authorization tokens expire, so refresh this secret before its token expires (typically within 12 hours). If you deploy to another namespace, create or refresh `aws-registry-key` in that namespace as well, or update the manifest to reference the correct secret name. Keep ECR credentials and Kubernetes secrets out of source control.

## Repository layout

- `Jenkinsfile` - declarative Jenkins CI/CD pipeline.
- `kubernetes/deployment.yaml` - EKS Deployment template using an ECR image.
- `kubernetes/service.yaml` - EKS Service template.
- `src/main` - Spring Boot application source.
- `src/test` - application tests.
- `Dockerfile` - application container image definition.
- `pom.xml` - Maven project definition.
