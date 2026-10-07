// def gv

pipeline {
    agent any
    tools{
        maven 'maven-3.9'
    }
    environment {
        EC2_HOST = '65.2.75.179'
        DOCKER_REPO_SERVER = '066949448876.dkr.ecr.ap-south-1.amazonaws.com'
        DOCKER_REPO = "${DOCKER_REPO_SERVER}/java-maven-app"
    }
    stages {
        stage("init") {
            steps {
                script {
                    echo "Initializing the script"
                    echo "Checking github integration.."
                    // gv = load "script.groovy"
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
                    echo "Building the application..."
                    sh 'mvn clean package'
                }
            }
        }
        stage("build image") {
            steps {
                script {
                    echo "Building the docker image..."
                    withCredentials([usernamePassword(credentialsId: 'ecr-credentials', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                        sh "docker build -t ${DOCKER_REPO}:${IMAGE_NAME} ."
                        sh "echo $PASSWORD | docker login -u $USERNAME --password-stdin ${DOCKER_REPO_SERVER}"
                        sh "docker push ${DOCKER_REPO}:${IMAGE_NAME}"
                    }
                }
            }
        }
        stage("deploy") {
            environment {
                AWS_ACCESS_KEY_ID = credentials('jenkins-aws_access_key_id')
                AWS_SECRET_ACCESS_KEY = credentials('jenkins-aws_secret_access_key')
                AWS_DEFAULT_REGION = "ap-south-1"
                APP_NAME = 'java-maven-app'
            }
            steps {
                script {
                    echo "Deploying to eks cluster"
                    sh 'envsubst < kubernetes/deployment.yaml | kubectl apply -f -'
                    sh 'envsubst < kubernetes/service.yaml | kubectl apply -f -'
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

                            git push https://github.com/AnantLuthra/Simple-Java-App.git HEAD:ci-cd-eks
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
