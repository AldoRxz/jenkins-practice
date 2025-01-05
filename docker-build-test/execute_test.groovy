def remote = [:]
remote.name = "${params.host_name}"
remote.host = "${params.host_ip}"
remote.branch = "${params.branch_name}"

pipeline {
    agent any

    environment {
        CRED = credentials('sandbox')
        DB = "${params.branch_name}_db" // Base de datos personalizada
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

        stage('MODIFY DOCKER-COMPOSE FILES') {
            steps {
                sshCommand remote: remote, command: """
                    cd /root;

                    docker exec -it api_${params.branch_name} python manage.py test api_app.tests.simple_test 
                
                """
            }
        }
    }
}
