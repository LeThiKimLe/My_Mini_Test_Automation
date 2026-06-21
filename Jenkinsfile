pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        choice(
            name: 'TEST_SUITE',
            choices: ['regression', 'smoke', 'all', 'custom'],
            description: 'Select test suite to run. Choose "custom" to specify a custom tag expression in CUSTOM_TEST_GROUP.'
        )
        string(
            name: 'CUSTOM_TEST_GROUP',
            defaultValue: 'release-2.0',
            description: 'Specify the custom JUnit tag expression (e.g. smoke & sprint-login). Only used if TEST_SUITE is "custom".'
        )
    }

    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        SELECTED_TEST_GROUP = ''
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Select Test Group') {
            steps {
                script {
                    // Fallback to default 'all' if parameters are not yet loaded (first run after Jenkinsfile change)
                    def testSuite = params.TEST_SUITE ?: 'all'
                    def customGroup = params.CUSTOM_TEST_GROUP ?: ''

                    if (testSuite == 'custom') {
                        if (!customGroup.trim()) {
                            error "TEST_SUITE is set to 'custom', but CUSTOM_TEST_GROUP is empty."
                        }
                        env.SELECTED_TEST_GROUP = customGroup.trim()
                        echo "Using custom test group: ${env.SELECTED_TEST_GROUP}"
                    } else {
                        env.SELECTED_TEST_GROUP = testSuite
                        echo "Using selected test suite: ${env.SELECTED_TEST_GROUP}"
                    }
                }
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

        stage('Run Tests') {
            steps {
                script {
                    runSelectedTestGroup(env.SELECTED_TEST_GROUP)
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/allure-report/**, target/allure-single/index.html, target/allure-results/**, target/site/allure-report/data/attachments/**, target/trace/**'
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



void runSelectedTestGroup(String testGroup) {
    if (!testGroup?.trim()) {
        error 'No test group selected.'
    }

    if (testGroup == 'all') {
        runAllTests()
    } else if (testGroup in ['smoke', 'regression']) {
        runMavenSuite(testGroup)
    } else {
        runMavenTagExpression(testGroup)
    }
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
        sh 'mvn -B clean test'
    } else {
        bat 'mvn -B clean test'
    }
}

void runMavenTagExpression(String tagExpression) {
    if (isUnix()) {
        sh "mvn -B clean test \"-Dtest.groups=${tagExpression}\""
    } else {
        bat "mvn -B clean test \"-Dtest.groups=${tagExpression}\""
    }
}
