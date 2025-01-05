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
                    cd /root/test/docker-tests;

                    # Modificar docker-compose.yml para usar el nuevo contexto del branch
                    sed -i "s|context: ./django-jenkins-demo|context: ./${params.branch_name}|" docker-compose.yml;
                    sed -i "s|container_name: django_api|container_name: api_${params.branch_name}|" docker-compose.yml;
                    sed -i "s|volumes:.*|volumes:\\n      - ./${params.branch_name}:/app|" docker-compose.yml;
                    sed -i "s|env_file:.*|env_file:\\n      - ./${params.branch_name}/local.env|" docker-compose.yml;

                """
            }
        }
    }
}
