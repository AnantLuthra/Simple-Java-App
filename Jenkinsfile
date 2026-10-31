library identifier: 'jenkins-shared-library@master', retriever: modernSCM(
    [$class: 'GitSCMSource',
    remote: 'https://github.com/AnantLuthra/jenkins-shared-library.git',
    credentialsId: 'git-credentials']
)

pipeline {   
    agent any
    tools{
        maven 'maven-3.9'
    }
    environment {
        IMAGE_NAME = 'anantluthra/simple-java-app:2.0'
    }
    stages {
        stage("init") {
            steps {
                script {
                    echo "Initializing the script"
                    gv = load "script.groovy"
                }
            }
        }
        stage("build jar") {
            steps {
                script {
                    buildJar()
                }
            }
        }
        stage("build & push image") {
            steps {
                script {
                    buildImage(env.IMAGE_NAME)
                    dockerLogin()
                    dockerPush(env.IMAGE_NAME)
                }
            }
        }
        
        stage("provision server"){
            environment{
                AWS_ACCESS_KEY_ID = credentials('jenkins-aws_access_key_id')
                AWS_SECRET_ACCESS_KEY = credentials('jenkins-aws_secret_access_key')
                TF_VAR_env_prefix = 'test'
            }
            steps{
                script{
                    dir('terraform'){
                        sh "terraform init"
                        sh "terraform apply --auto-approve"
                        EC2_PUBLIC_IP = sh(
                            script: "terraform output ec2_public_ip",
                            returnStdout: true
                        ).trim()
                    }
                }
            }
        }

        stage("deploy") {
            environment{
                DOCKER_CREDS = credentials('docker-hub-repo')
            }
            steps {
                script {
                    echo "Waiting for EC2 server to initialize..."
                    sleep(time: 90, unit: "SECONDS")

                    echo "Deploying the docker image to EC2 Server ..."
                    echo "EC2 IP: ${EC2_PUBLIC_IP}"

                    def docCmd = "bash ./server-cmds.sh ${env.IMAGE_NAME} ${env.DOCKER_CREDS_USR} ${env.DOCKER_CREDS_PSW}"
                    def ec2Instance = "ec2-user@${EC2_PUBLIC_IP}"

                    sshagent(credentials: ['ec2-ssh-key'], executable: '') {
                        sh "scp -o StrictHostKeyChecking=no docker-compose.yaml ${ec2Instance}:/home/ec2-user"
                        sh "scp -o StrictHostKeyChecking=no server-cmds.sh ${ec2Instance}:/home/ec2-user"
                        sh "ssh -o StrictHostKeyChecking=no ${ec2Instance} ${docCmd}"
                    }
                }
            }
        }               
    }
} 

