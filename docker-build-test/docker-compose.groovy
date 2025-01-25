def remote = [:]
remote.name = "${params.host_name}"
remote.host = "${params.host_ip}"
remote.branch = "${params.branch_name}"
remote.branchid = "${params.branch_id}"

pipeline {
    agent any

    environment {
        CRED = credentials('sandbox')
        DB = "${params.branch_name}_db" 
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
                    cd /root/test/docker-tests/${params.branch_name};

                    # Crear el archivo docker-compose.yml
                    cat > docker-compose.yml <<EOL
version: '3'

services:
    mysql:
        container_name: mysql_${params.branch_name}
        platform: linux/x86_64
        build:
            context: ./mysql
            dockerfile: Dockerfile
        ports:
            - "3307:3306"
        env_file:
            - ./mysql/local.env

    django-jenkins:
        depends_on:
            - mysql
        container_name: api_${params.branch_name}
        platform: linux/x86_64
        build:
            context: ./${params.branch_name}
            dockerfile: dev.Dockerfile
        restart: unless-stopped
        ports:
            - "8050:8050"

EOL

                """
            }
        }
    }
}