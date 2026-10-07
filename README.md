# Java Maven App — Jenkins EKS Deployment

## Project description

This branch focuses on deploying a workload to an AWS EKS cluster from Jenkins. The Jenkinsfile's functional work is in its `deploy` stage: it reads the AWS access-key credentials stored in Jenkins and uses `kubectl` to create an NGINX deployment (and therefore a pod) in the EKS cluster.

The `build app` and `build image` stages currently only print status messages; they do not build an artifact or image. This branch is intended as a Jenkins-to-EKS deployment example, not the complete Docker/Maven CI/CD flow.

## Jenkins pipeline

The `deploy` stage sets the following environment variables:

- `AWS_ACCESS_KEY_ID` from Jenkins credential `jenkins-aws_access_key_id`
- `AWS_SECRET_ACCESS_KEY` from Jenkins credential `jenkins-aws_secret_access_key`
- `AWS_DEFAULT_REGION` set to `ap-south-1`

It then runs:

```sh
kubectl create deployment nginx-deployment --image=nginx
```

Provided the Jenkins agent can authenticate to the configured EKS cluster, this creates the `nginx-deployment` Deployment, which creates an NGINX pod (or pods managed by that Deployment) in the cluster.

## Jenkins agent prerequisites

Before running this pipeline, prepare the Jenkins controller/agent that executes the job:

1. Install `kubectl` and make sure it is available on the job's `PATH`.
2. Install `aws-iam-authenticator` and ensure it is executable at `/usr/bin/aws-iam-authenticator`.
3. Create the Kubernetes configuration directory if it does not exist:

   ```sh
   mkdir -p /var/jenkins_home/.kube
   ```

4. Put the EKS kubeconfig in `/var/jenkins_home/.kube/config`.

Jenkins runs as the `jenkins` user in many installations, so ensure that user can read the config file and execute both `kubectl` and `aws-iam-authenticator`.

## EKS kubeconfig

The kubeconfig at `/var/jenkins_home/.kube/config` tells `kubectl` which cluster to contact and configures AWS IAM token authentication. Replace `<endpoint-url>` and `<cluster-name>` with the endpoint and name of your EKS cluster:

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

When `kubectl` connects to EKS, it invokes `aws-iam-authenticator` using this `exec` configuration. The authenticator uses the AWS credentials injected by Jenkins to obtain an IAM authentication token for the cluster. The IAM identity represented by those credentials must also be authorized in EKS/Kubernetes to create Deployments.

## Repository layout

- `Jenkinsfile` — Jenkins declarative pipeline that deploys NGINX to EKS.
- `src/` — Java application source (not built by this branch's current pipeline).
- `pom.xml` — Maven project definition.
- `Dockerfile`, `docker-compose.yaml`, `script.groovy`, and `server-cmds.sh` — supporting files retained from related pipeline examples.
