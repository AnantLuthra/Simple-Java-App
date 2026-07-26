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
                    env.JAR_HEALTH = input message: "Is built jar working fine?", parameters: [choice(name: "ONE", choices: ["Yes", "No"], description: "")]
                    echo "JAR Health: ${env.JAR_HEALTH}"
                    gv.build_image()
                }
            }
        }
        stage("deploy") {
            input{
                message "What should we do with this build?"
                parameters {
                    choice(name: "Deploy", choices: ["Yes", "No"], description: "Should we proceed?")
                    booleanParam(name: "SAVE_ARTIFACTS", defaultValue: true, description: "Should we save artifacts?")
                }
            }
            when {
                expression{
                    env.Deploy == "Yes"
                }
            }
            steps {
                // echo "Credentials got through env. - ${SERVER_CREDS}"
                echo "deploying"
                echo "Save artifacts is: ${env.SAVE_ARTIFACTS}"
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
