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
            steps {
                script {
                    echo "deploying"
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
