def gv

pipeline {
    agent any
    tools{
        maven 'maven-3.9'
    }
    environment {
        EC2_HOST = '65.2.75.179'
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
                    echo "Deploying the docker image to EC2 Server ..."
                    def docCmd = "bash ./server-cmds.sh anantluthra/simple-java-app:${IMAGE_NAME}"
                    sshagent(credentials: ['ec2-server-key'], executable: '') {
                        sh "scp docker-compose.yaml ec2-user@${EC2_HOST}:/home/ec2-user"
                        sh "scp server-cmds.sh ec2-user@${EC2_HOST}:/home/ec2-user"
                        sh "ssh -o StrictHostKeyChecking=no ec2-user@${EC2_HOST} ${docCmd}"
                    }
                }
            }
        }    
        stage("commit version update") {
    steps {
        script {
            withCredentials([gitUsernamePassword(
                credentialsId: 'git-credentials',
                gitToolName: 'Default'
            )]) {
                sh '''
                    git config user.email "jenkins@example.com"
                    git config user.name "jenkins"

                    git add .
                    git commit -m "ci: version bump"

                    git push https://github.com/AnantLuthra/Simple-Java-App.git HEAD:master
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
