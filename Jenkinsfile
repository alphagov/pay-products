#!/usr/bin/env groovy

pipeline {
  agent any

  options {
    timestamps()
  }

  libraries {
    lib("pay-jenkins-library@master")
  }

  environment {
    DOCKER_HOST = "unix:///var/run/docker.sock"
    AWS_DEFAULT_REGION = "eu-west-1"
  }

  stages {
    stage('Maven Build') {
      steps {
        sh 'docker pull govukpay/postgres:9.4.4'
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
    stage('Test') {
      steps {
        runParameterisedEndToEnd("products", null, "end2end-tagged", false, false, "uk.gov.pay.endtoend.categories.End2EndProducts")
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
        deploy("products", "test", null, false, false, "uk.gov.pay.endtoend.categories.SmokeProducts")
      }
    }
  }
}
