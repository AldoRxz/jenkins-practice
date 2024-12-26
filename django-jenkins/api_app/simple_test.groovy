
pipeline {

    agent any


    stages {
        stage('Setup') {
            steps {
                dir('/workspace/django-jenkins-demo') {
                    script {
                        sh 'source venv/Scripts/activate || .\venv\Scripts\activate'
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
