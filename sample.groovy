def init(){
    echo "Hello from ${NAME}"
    echo "Init stage started..."
}

def testing(){
    echo "Running tests..."
}

def build_image(){
    echo "Now with credentials wrapper"
    withCredentials([
        usernamePassword(credentialsId: 'server-credentials', usernameVariable: 'USER', passwordVariable: 'PASSW')
    ]) {
        sh 'echo "Usern and passw got through with creds - $USER"'
    }
    echo "building image"
}

export this