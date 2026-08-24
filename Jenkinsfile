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
                    echo "Checking github integration.."
                    gv = load "script.groovy"
                }
            }
        }
        stage("build jar") {
            steps {
                script {
                    gv.buildJar()
                }
            }
        }
        stage("build image") {
            steps {
                script {
                    gv.buildImage()
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
