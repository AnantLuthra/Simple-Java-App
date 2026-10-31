# AWS-Hosted Java Application CI/CD Pipeline

This repository contains a Spring Boot application, its Docker image, and a
Jenkins pipeline that builds, publishes, provisions, and deploys the
application to AWS.

## What this project does

- Builds and tests a Java 17 Spring Boot application with Maven.
- Packages the application as a Docker image and publishes it to Docker Hub.
- Provisions the AWS networking and EC2 infrastructure with Terraform.
- Installs Docker and Docker Compose on the provisioned EC2 instance.
- Deploys the application and PostgreSQL services to EC2 using Docker Compose.
- Exposes the application through its web UI and `/api/status` endpoint.
- Automates the complete build, infrastructure, and deployment workflow through
  Jenkins and a reusable shared library.
- The application runs on Java 17 with Spring Boot 3.5.5, uses port `8080`,
  and is packaged in an `amazoncorretto:17-alpine-jdk` container.

## CI/CD pipeline

The [`Jenkinsfile`](./Jenkinsfile) runs these stages:

1. **init** - Loads [`script.groovy`](./script.groovy) and initializes the
   pipeline context.
2. **build jar** - Builds the application with the shared library's
   `buildJar()` step.
3. **build & push image** - Builds and pushes
   `anantluthra/simple-java-app:2.0` to Docker Hub.
4. **provision server** - Runs Terraform from [`terraform/`](./terraform/) to
   provision the AWS network and EC2 server, then reads the `ec2_public_ip`
   output.
5. **deploy** - Waits for the server to initialize, copies
   [`docker-compose.yaml`](./docker-compose.yaml) and
   [`server-cmds.sh`](./server-cmds.sh) to the EC2 host, then authenticates to
   Docker Hub and starts the application containers.

The pipeline uses the Jenkins shared library
[`jenkins-shared-library`](https://github.com/AnantLuthra/jenkins-shared-library)
for `buildJar`, `buildImage`, `dockerLogin`, and `dockerPush`.

## Repository layout

```text
.
├── Jenkinsfile                 # Jenkins CI/CD pipeline
├── Dockerfile                  # Application container definition
├── docker-compose.yaml          # Application and PostgreSQL services
├── server-cmds.sh              # Remote Docker deployment commands
├── script.groovy               # Jenkins pipeline helper
├── pom.xml                     # Maven project configuration
├── src/
│   ├── main/                   # Spring Boot application source and UI
│   └── test/                   # Application tests
└── terraform/
    ├── main.tf                 # AWS resources and outputs
    ├── variables.tf            # Terraform variable defaults
    └── entry-script.sh         # EC2 bootstrap script
```

## AWS infrastructure

Terraform provisions the following resources in AWS:

- VPC (`10.0.0.0/16` by default)
- Public subnet (`10.0.10.0/24` in `ap-south-1b` by default)
- Internet gateway
- Default route table with a `0.0.0.0/0` route
- Default security group
- Amazon Linux EC2 instance (`t3.micro` by default)
- EC2 key pair

The EC2 bootstrap script [`terraform/entry-script.sh`](./terraform/entry-script.sh)
installs Docker and Docker Compose. The security group allows:

- SSH (`22`) from the configured workstation and Jenkins IP addresses
- Application traffic (`8080`) from the internet
- All outbound traffic

Review the security-group CIDR values before provisioning. The current values
in [`terraform/variables.tf`](./terraform/variables.tf) are environment-specific
and should not be treated as portable defaults.

## Prerequisites

### Jenkins

Run Jenkins on a server such as a DigitalOcean Droplet. Jenkins may run
directly on the host or as a Docker container. The Jenkins host or container
must have access to Docker and the tools required by the pipeline.

Install or make available in the Jenkins runtime:

- Maven tool named `maven-3.9`
- JDK 17
- Git
- Docker CLI and permission to access the Docker daemon
- Terraform 1.x
- Bash and standard SSH utilities (`ssh`, `scp`, and `ssh-agent`)

The AWS IAM Authenticator is not required for this project because Terraform
uses AWS access-key credentials to provision EC2 resources rather than
connecting to an Amazon EKS cluster.

Configure the following Jenkins items:

- Shared library named `jenkins-shared-library`
- Docker Hub credential: `docker-hub-repo`
- AWS access-key credential: `jenkins-aws_access_key_id`
- AWS secret-key credential: `jenkins-aws_secret_access_key`
- Git credential: `git-credentials`
- Jenkins credential `ec2-ssh-key` of type **SSH Username with private key**
- Terraform installed and available on the Jenkins agent

The pipeline sets `TF_VAR_env_prefix=test` for the provisioning stage. Other
Terraform values are currently supplied by [`terraform/variables.tf`](./terraform/variables.tf).

Before running the pipeline, create or select an EC2 SSH key pair named
`ssh-server-key` in the configured AWS region. Add its matching private key to
Jenkins as an **SSH Username with private key** credential with:

- **ID:** `ec2-ssh-key`
- **Username:** `ec2-user`
- **Private key:** the private key matching the EC2 key pair

Terraform uses the existing key pair when it creates the EC2 instance. No
separate manual Terraform provisioning step is required.

For a Docker-based Jenkins installation, mount the Docker socket into the
Jenkins container or use a dedicated Jenkins agent with Docker access. Install
Terraform, Java, Maven, Git, and the SSH client on that agent rather than
assuming they exist in the controller container.

The Jenkins server needs outbound network access to GitHub, Docker Hub, AWS,
and the shared-library repository. The provisioned EC2 security group must
allow SSH from the Jenkins server's public IP and application traffic on port
`8080`.

## Infrastructure and deployment operation

The Jenkins pipeline handles the complete infrastructure and deployment
workflow automatically:

1. Terraform initializes in the [`terraform/`](./terraform/) directory.
2. Terraform provisions the VPC, subnet, internet gateway, route table,
   security group, and EC2 instance.
3. The pipeline reads the `ec2_public_ip` Terraform output.
4. Jenkins waits for the EC2 bootstrap script to install Docker and Docker
   Compose.
5. Jenkins connects to the instance as `ec2-user` using the `ec2-ssh-key`
   credential and the AWS key pair named `ssh-server-key`.
6. Jenkins copies the Compose and deployment scripts to EC2 and starts the
   application and PostgreSQL containers.

The only AWS setup required before the pipeline runs is an AWS account with
the required Jenkins credentials and an EC2 key pair named `ssh-server-key`.
Review the Terraform defaults before changing environments; the pipeline
currently applies the configuration automatically with
`TF_VAR_env_prefix=test`.

## Jenkins and Terraform wiring

The provisioning and deployment stages are aligned as follows:

- Terraform exposes the EC2 address as `ec2_public_ip`.
- Jenkins reads that output into `EC2_PUBLIC_IP`.
- The deploy stage uses `EC2_PUBLIC_IP` for both SSH and file transfer.
- Terraform assigns the existing AWS key pair `ssh-server-key` to the EC2
  instance.
- Jenkins uses the matching private key through the `ec2-ssh-key` credential.
- Terraform variables use explicit `default` values, and no undefined
  `public_key_location` variable is required.

The AWS key pair `ssh-server-key` and Jenkins credential `ec2-ssh-key` are
different resources: the former is the key-pair name registered in AWS, while
the latter stores its matching private key for Jenkins. The EC2 username is
`ec2-user`.

## Security notes

- Do not commit AWS keys, Docker Hub passwords, private keys, or Terraform
  state files.
- Restrict SSH ingress to trusted IP addresses.
- Replace the example PostgreSQL password in
  [`docker-compose.yaml`](./docker-compose.yaml) before using this setup outside
  a learning environment.
- Consider storing Terraform state remotely with locking for shared
  environments.
