pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        choice(
            name: 'TEST_GROUP_PRESET',
            choices: [
                'smoke',
                'regression',
                'all',
                'smoke & sprint-login',
                'regression & sprint-login',
                'regression & release-1.0'
            ],
            description: 'Choose a predefined test group expression'
        )
        string(name: 'CUSTOM_TEST_GROUPS', defaultValue: '', description: 'Optional advanced JUnit tag expression. If filled, this overrides TEST_GROUP_PRESET.')
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
                expression { !params.CUSTOM_TEST_GROUPS?.trim() && params.TEST_GROUP_PRESET == 'smoke' }
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
                expression { !params.CUSTOM_TEST_GROUPS?.trim() && params.TEST_GROUP_PRESET == 'regression' }
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
                expression { getSelectedTagExpression() }
            }
            steps {
                runMavenTagExpression(getSelectedTagExpression())
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

        stage('All Tests') {
            when {
                expression { !params.CUSTOM_TEST_GROUPS?.trim() && params.TEST_GROUP_PRESET == 'all' }
            }
            steps {
                runAllTests()
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

String getSelectedTagExpression() {
    if (params.CUSTOM_TEST_GROUPS?.trim()) {
        return params.CUSTOM_TEST_GROUPS.trim()
    }

    if (params.TEST_GROUP_PRESET in ['smoke', 'regression', 'all']) {
        return ''
    }

    return params.TEST_GROUP_PRESET
}

void runMavenSuite(String suite) {
    if (isUnix()) {
        sh "mvn -B clean test -P${suite}"
    } else {
        bat "mvn -B clean test -P${suite}"
    }
}

void runAllTests() {
    if (isUnix()) {
        sh "mvn -B clean test"
    } else {
        bat "mvn -B clean test"
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
