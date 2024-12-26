
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
