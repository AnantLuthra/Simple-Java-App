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

        stage("deploy") {
            steps {
                script {
                    echo "Deploying the docker image to EC2 Server ..."
                    def docCmd = "bash ./server-cmds.sh ${env.IMAGE_NAME}"
                    sshagent(credentials: ['ec2-server-key'], executable: '') {
                        sh "scp docker-compose.yaml ec2-user@13.204.46.200:/home/ec2-user"
                        sh "scp server-cmds.sh ec2-user@13.204.46.200:/home/ec2-user"
                        sh "ssh -o StrictHostKeyChecking=no ec2-user@13.204.46.200 ${docCmd}"
                    }
                }
            }
        }               
    }
} 

