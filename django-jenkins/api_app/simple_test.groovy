
pipeline {

    agent any


    stages {
        stage('Setup') {
            steps {
                dir('/workspace/django-jenkins-demo') {
                    script {
                        sh 'python -m venv venv'
                        sh 'source venv/bin/activate || .\\venv\\Scripts\\activate'
                    }
                }
            }
        }
        stage('Run Tests') {
            steps {
                dir('/workspace/django-jenkins-demo') {
                    sh 'python manage.py test api_app.tests.simple_test'
                }
            }
        }
    }
}


def remote = [:]
remote.name = "${params.host_name}"
remote.host = "${params.host_ip}"

pipeline {
    agent any

    environment {
        CRED = credentials('sandbox')
    }

    stages {
        stage('SET CRED') {
            steps {
                script {
                    remote.user = 'root'
                    remote.password = "${CRED_PSW}"
                    remote.allowAnyHosts = true
                }
            }
        }

        stage('Check Python Version') {
            steps {
                sshCommand remote: remote,
                    command: "python3 --version"
            }
        }

    }

    post {
        success {
            echo 'Fase success'
        }
        failure {
            echo 'Fase failure'
        }
    }
}