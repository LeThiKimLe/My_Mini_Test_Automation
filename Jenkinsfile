pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        string(
            name: 'CUSTOM_TEST_GROUPS',
            defaultValue: '',
            description: 'Optional advanced JUnit tag expression. If filled, Jenkins skips the dynamic picker.'
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
                    if (params.CUSTOM_TEST_GROUPS?.trim()) {
                        env.SELECTED_TEST_GROUP = params.CUSTOM_TEST_GROUPS.trim()
                        echo "Using custom test group expression: ${env.SELECTED_TEST_GROUP}"
                    } else {
                        def choices = loadTestGroupChoices()
                        env.SELECTED_TEST_GROUP = input(
                            message: 'Choose test group to run',
                            ok: 'Run tests',
                            parameters: [
                                choice(
                                    name: 'TEST_GROUP',
                                    choices: choices.join('\n'),
                                    description: 'Loaded from ci/test-groups.txt'
                                )
                            ]
                        )
                        echo "Selected test group: ${env.SELECTED_TEST_GROUP}"
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

List<String> loadTestGroupChoices() {
    def groupsFile = 'ci/test-groups.txt'
    if (!fileExists(groupsFile)) {
        error "Missing ${groupsFile}. Add one test group expression per line."
    }

    def choices = readFile(groupsFile)
        .readLines()
        .collect { it.trim() }
        .findAll { it && !it.startsWith('#') }

    if (choices.isEmpty()) {
        error "${groupsFile} does not contain any test group choices."
    }

    return choices
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
