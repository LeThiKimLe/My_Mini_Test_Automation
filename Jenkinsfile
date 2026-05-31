pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install Playwright Chromium') {
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                            mvn -B org.codehaus.mojo:exec-maven-plugin:3.1.0:java \
                              -Dexec.mainClass=com.microsoft.playwright.CLI \
                              -Dexec.args="install --with-deps chromium"
                        '''
                    } else {
                        bat '''
                            mvn -B org.codehaus.mojo:exec-maven-plugin:3.1.0:java ^
                              -Dexec.mainClass=com.microsoft.playwright.CLI ^
                              -Dexec.args="install chromium"
                        '''
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn -B clean test'
                    } else {
                        bat 'mvn -B clean test'
                    }
                }
            }
        }

    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/allure-report/**, target/allure-single/index.html, target/allure-results/**, target/screenshots/**, target/trace/**'
            allure includeProperties: false, results: [[path: 'target/allure-results']]
        }
    }
}
