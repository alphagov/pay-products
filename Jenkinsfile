#!/usr/bin/env groovy

pipeline {
  agent any

  parameters {
    booleanParam(defaultValue: false, description: '', name: 'runEndToEndTestsOnPR')
  }

  options {
    timestamps()
  }

  libraries {
    lib("pay-jenkins-library@master")
  }

  environment {
    DOCKER_HOST = "unix:///var/run/docker.sock"
    AWS_DEFAULT_REGION = "eu-west-1"
    RUN_END_TO_END_ON_PR = "${params.runEndToEndTestsOnPR}"
    JAVA_HOME="/usr/lib/jvm/java-1.11.0-openjdk-amd64"
  }

  stages {
    stage('Maven Build') {
      steps {
        script {
          long stepBuildTime = System.currentTimeMillis()
          sh 'mvn -version'
          sh 'mvn clean verify'
          runProviderContractTests()
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
    stage('Complete') {
      failFast true
      parallel {
        stage('Tag Build') {
          when {
            branch 'master'
          }
          steps {
            tagDeployment("products")
          }
        }
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
