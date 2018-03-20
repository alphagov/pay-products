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
        script {
          def long stepBuildTime = System.currentTimeMillis()

          sh 'docker pull govukpay/postgres:9.4.4'
          sh 'mvn clean package'

          postSuccessfulMetrics("products.maven-build", stepBuildTime)
        }
      }
      post {
        failure {
          postMetric("products.maven-build.failure", 1)
        }
      }
    }
    stage('Docker Build') {
      steps {
        script {
          buildAppWithMetrics {
            app = "products"
          }
        }
      }
      post {
        failure {
          postMetric("products.docker-build.failure", 1)
        }
      }
    }
    stage('Test') {
      steps {
        runProductsE2E("products")
      }
    }
    stage('Docker Tag') {
      steps {
        script {
          dockerTagWithMetrics {
            app = "products"
          }
        }
      }
      post {
        failure {
          postMetric("products.docker-tag.failure", 1)
        }
      }
    }
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        deployEcs("products", "test", null, true, true, "uk.gov.pay.endtoend.categories.SmokeProducts", true)
      }
    }
  }
  post {
    failure {
      postMetric(appendBranchSuffix("products") + ".failure", 1)
    }
    success {
      postSuccessfulMetrics(appendBranchSuffix("products"))
    }
  }
}
