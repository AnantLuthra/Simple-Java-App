def gv

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
                script{
                    gv = load "sample.groovy"
                    gv.init()
                }
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
                    gv.testing()
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
                script{
                    gv.build_image()
                }
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
