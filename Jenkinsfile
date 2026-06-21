pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        choice(name: 'TEST_SUITE', choices: ['smoke', 'regression', 'all'], description: 'Test suite to run')
        string(name: 'TEST_GROUPS', defaultValue: '', description: 'Optional JUnit tag expression, for example: regression & sprint-login')
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
                              -Dexec.args="install chromium"
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

        stage('Smoke Tests') {
            when {
                expression { !params.TEST_GROUPS?.trim() && (params.TEST_SUITE == 'smoke' || params.TEST_SUITE == 'all') }
            }
            steps {
                runMavenSuite('smoke')
            }
            post {
                always {
                    allure([
                        commandline: 'allure',
                        includeProperties: false,
                        results: [[path: 'target/allure-results']]
                    ])
                }
            }
        }

        stage('Regression Tests') {
            when {
                expression { !params.TEST_GROUPS?.trim() && (params.TEST_SUITE == 'regression' || params.TEST_SUITE == 'all') }
            }
            steps {
                runMavenSuite('regression')
            }
            post {
                always {
                    allure([
                        commandline: 'allure',
                        includeProperties: false,
                        results: [[path: 'target/allure-results']]
                    ])
                }
            }
        }

        stage('Custom Tagged Tests') {
            when {
                expression { params.TEST_GROUPS?.trim() }
            }
            steps {
                runMavenTagExpression(params.TEST_GROUPS.trim())
            }
            post {
                always {
                    allure([
                        commandline: 'allure',
                        includeProperties: false,
                        results: [[path: 'target/allure-results']]
                    ])
                }
            }
        }
    }
}

void runMavenSuite(String suite) {
    if (isUnix()) {
        sh "mvn -B clean test -P${suite}"
    } else {
        bat "mvn -B clean test -P${suite}"
    }
}

void runMavenTagExpression(String tagExpression) {
    if (isUnix()) {
        sh "mvn -B clean test \"-Dtest.groups=${tagExpression}\""
    } else {
        bat "mvn -B clean test \"-Dtest.groups=${tagExpression}\""
    }
}

void publishSuiteArtifacts(String suite) {
    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/allure-report/**, target/allure-single/index.html, target/allure-results/**, target/site/allure-report/data/attachments/**, target/trace/**'
    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/allure-single/index.html', fingerprint: true
    allure includeProperties: false, reportBuildPolicy: 'ALWAYS', results: [[path: 'target/allure-results']]
}
