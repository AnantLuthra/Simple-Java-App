pipeline {
    agent any
    tools {
        maven 'maven-3.9'
    }
    stages {
        stage("build app") {
            steps {
                script {
                    echo "building app"
                }
            }
        }
        stage("build image") {
            steps {
                script {
                    echo "building image"
                }
            }
        }
        stage("deploy") {
            environment {
                AWS_ACCESS_KEY_ID = credentials('jenkins-aws_access_key_id')
                AWS_SECRET_ACCESS_KEY = credentials('jenkins-aws_secret_access_key')
                AWS_DEFAULT_REGION = "ap-south-1"
            }
            steps {
                script {
                    echo "deploying"
                    sh 'kubectl create deployment nginx-deployment --image=nginx'
                }
            }
        }
    }
    post {
        success {
            echo "All Success!"
        }
        failure {
            echo "Something went wrong..."
        }
    }
}
