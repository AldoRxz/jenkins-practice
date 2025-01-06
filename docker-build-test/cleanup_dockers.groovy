def remote = [:]
remote.name = "${params.host_name}"
remote.host = "${params.host_ip}"
remote.branch = "${params.branch_name}"

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

        stage('CLEAN DOCKERS CONTAINERS') {
            steps {
                sshCommand remote: remote, command: """
                    cd /root/test/docker-tests;

                    docker stop api_${params.branch_name}  
                    docker stop mysql_${params.branch_name}
                    docker rm api_${params.branch_name}
                    docker rm mysql_${params.branch_name}
                    docker rmi api_${params.branch_name}
                    docker rmi mysql_${params.branch_name}

                """
            }
        }
        stage('CLEAN PROYECT FOLDER') {
            steps {
                sshCommand remote: remote, command: """
                    cd /root/test/docker-tests;

                    rm -rf ${params.branch_name}

                """
            }
        }
        stage('CLEAN MYSQL FILES') {
            steps {
                sshCommand remote: remote, command: """
                    cd /root/test/docker-tests/mysql;

                    rm local.env
                    rm init.sql

                """
            }
        }
        stage('CLEAN DOCKER-COMPOSE FILES') {
            steps {
                sshCommand remote: remote, command: """
                    cd /root/test/docker-tests;

                    rm docker-compose.yml

                """
            }
        }
    }
}
