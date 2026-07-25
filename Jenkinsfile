// def gv

pipeline {
    agent any
    environment {
        NAME = 'jenkins-pro'
        SERVER_CREDS = credentials('server-credentials')
    }
    stages {
        stage("init") {
            steps {
                echo "Hello from ${NAME}"
                echo "Init stage started..."
            }
        }
        stage("build jar") {
            steps {
                echo "building stage"
            }
        }
        stage("build image") {
            steps {
                echo "Now with credentials wrapper"
                withCredentials([
                    usernamePassword(credentialsId: 'server-credentials', usernameVariable: 'USER', passwordVariable: 'PASSW')
                ]) {
                    sh "Usern & passw got through 'with creds' - $USER, $PASSW"
                }
                echo "building image"
            }
        }
        stage("deploy") {
            steps {
                echo "Credentials got through env. - ${SERVER_CREDS}"
                echo "deploying"
            }
        }
    }   
    post {
        always{
            echo "This is always done."
        }
        failure{
            echo "on failure"
        }
        success{
            echo "on success"
        }
    }
}
