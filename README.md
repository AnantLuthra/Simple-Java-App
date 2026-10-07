# Java Maven App - Jenkins LKE Deployment

## Project description

This branch demonstrates deploying a Kubernetes workload from Jenkins to Linode Kubernetes Engine (LKE). Its functional pipeline work is in the `deploy` stage: Jenkins obtains the LKE kubeconfig stored in its credentials, makes it available to `kubectl` for the duration of the deployment, and creates an NGINX Deployment in the LKE cluster.

The `build app` and `build image` stages currently only print status messages. They do not build an artifact or container image, so this branch is a Jenkins-to-LKE deployment example rather than a complete Maven/Docker CI/CD pipeline.

## Jenkins pipeline

The deploy stage uses the Jenkins Kubernetes CLI plugin's `withKubeConfig` step:

```groovy
withKubeConfig([
  credentialsId: 'lke-credentials',
  serverUrl: 'https://<lke-cluster-endpoint>'
]) {
    sh 'kubectl create deployment nginx-deployment --image=nginx'
}
```

`withKubeConfig` creates a temporary kubeconfig for the enclosed shell command. `kubectl` then connects to the configured LKE API server using the Kubernetes credentials saved in Jenkins. The command creates `nginx-deployment`, which manages an NGINX pod (or pods) in the LKE cluster.

## Jenkins prerequisites

Configure the Jenkins controller or agent that runs this job with the following:

1. Install `kubectl` and make it available on the job's `PATH`.
2. Install the Jenkins **Kubernetes CLI** plugin, which provides the `withKubeConfig` pipeline step.
3. Add Jenkins credentials with the ID `lke-credentials`. Store the kubeconfig for the target LKE cluster in this credential.
4. Ensure the Kubernetes identity in that kubeconfig is authorized to create Deployments in the destination LKE cluster and namespace.

Unlike the AWS EKS branch, this pipeline does not use AWS access-key credentials, `aws-iam-authenticator`, or a manually maintained `/var/jenkins_home/.kube/config` file. The `withKubeConfig` step supplies the kubeconfig temporarily and only within the deployment block.

## Getting the LKE kubeconfig

Download the kubeconfig for the desired LKE cluster from Linode Cloud Manager, then save its contents as the Jenkins credential named `lke-credentials`. Keep the kubeconfig secret: it contains the cluster endpoint and authentication material.

Replace `https://<lke-cluster-endpoint>` in the Jenkinsfile with the API endpoint of the LKE cluster you want to deploy to. Update the `lke-credentials` kubeconfig at the same time so both point to that cluster.

## Repository layout

- `Jenkinsfile` - declarative Jenkins pipeline that deploys NGINX to LKE.
- `src/` - Java application source, not built by this branch's current pipeline.
- `pom.xml` - Maven project definition.
- `Dockerfile`, `docker-compose.yaml`, `script.groovy`, and `server-cmds.sh` - supporting files retained from related pipeline examples.
