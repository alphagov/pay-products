#!/usr/bin/env groovy

pipeline {
  agent any

  options {
    ansiColor('xterm')
    timestamps()
  }

  libraries {
    lib("pay-jenkins-library@master")
  }

  environment {
    DOCKER_HOST = "unix:///var/run/docker.sock"
  }

  stages {
    stage('Maven Build') {
      steps {
        sh 'mvn clean package'
      }
    }
    stage('Docker Build') {
      steps {
        script {
          buildApp{
            app = "products"
          }
        }
      }
    }
    stage('Docker Tag') {
      steps {
        script {
          dockerTag {
            app = "products"
          }
        }
      }
    }
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        deployPaas("products", "test", null, true)
      }
    }
  }
}
