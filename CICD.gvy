pipeline {
    agent any
    environment {
        //be sure to replace "elchen8923" with your own Docker Hub username
        DOCKER_IMAGE_NAME = "elchen8923/train-schedule"
    }
    stages {
        stage('Checkout the app from github') {
            steps {
              echo 'checkout the app..'
              git branch: 'master', url: 'https://github.com/el-chen/cicd-pipeline-train-schedule-autodeploy'
           }
        }
        stage('Build the app') {
            steps {
                echo 'Running build automation'
                sh './gradlew build --no-daemon'
                archiveArtifacts artifacts: 'dist/trainSchedule.zip'
            }
        }
        stage('Build & Push docker image') {
            steps {
              withDockerRegistry(credentialsId: 'dockerhub_cred', url: 'https://index.docker.io/v1/') {
                    sh script: 'cd  $WORKSPACE'
                    sh script: 'docker build --file Dockerfile --tag docker.io/elchen8923/train-schedule:$BUILD_NUMBER .'
                    sh script: 'docker push docker.io/elchen8923/train-schedule:$BUILD_NUMBER'
              }	
            }
        }
        stage('Deploy the app to prod Kube master') {
           steps {
              sh 'sudo ansible-playbook --inventory /etc/ansible/hosts train-schedule-kube-deploy.yml --extra-vars "env=prod-kube-master  build=$BUILD_NUMBER"'
           }
        }
    }
}
