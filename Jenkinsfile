pipeline {
  agent {
    docker {
      image 'maven:3-openjdk-11-slim'
    }

  }
  stages {
    stage('') {
      steps {
        sh 'docker build -t censodev/udacity_java-web-4 .'
      }
    }

  }
}