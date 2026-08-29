def buildJar() {
    echo "Building the application..."
    sh 'mvn clean package'
}

def buildImage() {
    echo "Building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'docker-hub-repo', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
        sh "docker build -t anantluthra/simple-java-app:${IMAGE_NAME} ."
        sh "echo $PASSWORD | docker login -u $USERNAME --password-stdin"
        sh "docker push anantluthra/simple-java-app:${IMAGE_NAME}"
    }
} 

def deployApp() {
    echo "Deploying the docker image to EC2 Server..."
    def docCmd = "bash ./server-cmds.sh anantluthra/simple-java-app:${IMAGE_NAME}"
    sshagent(credentials: ['ec2-server-key'], executable: '') {
        sh "scp -o StrictHostKeyChecking=no docker-compose.yaml ec2-user@${EC2_HOST}:/home/ec2-user"
        sh "scp -o StrictHostKeyChecking=no server-cmds.sh ec2-user@${EC2_HOST}:/home/ec2-user"
        sh "ssh -o StrictHostKeyChecking=no ec2-user@${EC2_HOST} ${docCmd}"
    }
} 

return this
