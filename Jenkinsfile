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
                    withKubeConfig([credentialsId: 'lke-credentials', serverUrl: 'https://c412ee51-015d-4cc7-b3a1-1432aef8a43c.ap-west-1-gw.linodelke.net']){
                        sh 'kubectl create deployment nginx-deployment --image=nginx'
                    }
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
