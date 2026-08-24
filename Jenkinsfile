// library identifier: 'jenkins-shared-library@master', retriever: modernSCM(
//     [$class: 'GitSCMSource',
//     remote: 'https://github.com/AnantLuthra/jenkins-shared-library.git',
//     credentialsId: 'git-credentials']
// )

@Library('jenkins-shared-library')
def gv

pipeline {
    agent any
    tools{
        maven 'maven-3.9'
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
                    buildImage 'anantluthra/simple-java-app:2.0'
                    dockerLogin()
                    dockerPush 'anantluthra/simple-java-app:2.0'
                }
            }
        }
        stage("deploy") {
            steps {
                script {
                    echo "deploying"
                    gv.deployApp()
                }
            }
        }
    }
    post {
        success{
            echo "All Success!"
        }
        failure{
            echo "Something went wrong..."
        }
    }  
}
