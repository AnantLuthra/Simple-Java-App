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
    echo 'Deploying the application...'
} 

return this
