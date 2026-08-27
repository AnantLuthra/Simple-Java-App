// library identifier: 'jenkins-shared-library@master', retriever: modernSCM(
//     [$class: 'GitSCMSource',
//     remote: 'https://github.com/AnantLuthra/jenkins-shared-library.git',
//     credentialsId: 'git-credentials']
// )

pipeline {   
    agent any
    stages {
        stage("test") {
            steps {
                script {
                    echo "Testing the application..."

                }
            }
        }
        stage("build") {
            steps {
                script {
                    echo "Building the application..."
                }
            }
        }

        stage("deploy") {
            steps {
                script {
                    def docCmd = 'docker run -p 3000:3080 -d anantluthra/nodejs-app:1.0'
                    sshagent(credentials: ['ec2-server-key'], executable: '') {
                        "ssh -o StrictHostKeyChecking=no ec2-user@13.204.46.200 ${docCmd}"
                    }
                }
            }
        }               
    }
} 

