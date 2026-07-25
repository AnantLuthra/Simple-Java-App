// def gv

pipeline {
    agent any
    environment {
        NAME = 'jenkins-pro'
        // SERVER_CREDS = credentials('server-credentials')
    }
    parameters {
        string(name: "NAME", defaultValue: "Rockers", description: "just a name.")
        choice(name: "Build type", choices: ["Half", "Full"], description: "Amount of build you're doing.")
        booleanParam(name: "executeTests", defaultValue: true, description: "")
    }
    stages {
        stage("init") {
            steps {
                echo "Hello from ${NAME}"
                echo "Init stage started..."
            }
        }
        stage("testing") {
            when{
                expression{
                    params.executeTests
                }
            }
            steps{
                script{
                    echo "Running tests..."
                }
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
                    sh 'echo "Usern and passw got through with creds - $USER"'
                }
                echo "building image"
            }
        }
        stage("deploy") {
            steps {
                // echo "Credentials got through env. - ${SERVER_CREDS}"
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
