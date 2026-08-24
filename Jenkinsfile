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
        stage("increment version"){
            steps {
                script {
                    echo 'Incrementing Version...'
                    sh 'mvn build-helper:parse-version versions:set -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} versions:commit'
                    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
                    def version = matcher[0][1]
                    env.IMAGE_NAME = "$version-$BUILD_NUMBER"
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
        stage("commit version update") {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'git-credentials',
                        usernameVariable: 'USERNAME',
                        passwordVariable: 'PASSWORD'
                    )]) {

                        sh '''
                            git config user.email "jenkins@example.com"
                            git config user.name "jenkins"

                            git add .
                            git commit -m "ci: version bump"

                            git -c credential.helper='!f() { echo username=$USERNAME; echo password=$PASSWORD; }; f' \
                            push https://github.com/AnantLuthra/Simple-Java-App.git HEAD:master
                        '''
                    }
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
