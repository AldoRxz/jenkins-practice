def remote = [:]
remote.name = "${params.host_name}"
remote.host = "${params.host_ip}"
remote.branch = "${params.branch_name}"

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
                    cd /root/test/docker-tests;


                    # mysql
                    sed -i "s|container_name: mysql|container_name: mysql_${params.branch_name}|" docker-compose.yml;

                    # api
                    sed -i "s|container_name: api|container_name: api_${params.branch_name}|" docker-compose.yml;

                    sed -i "s|context: ./api|context: ./${params.branch_name}|" docker-compose.yml;

                    sed -i "s|volumes:.*|volumes:\\n      - ./${params.branch_name}:/app|" docker-compose.yml;
                    sed -i "s|env_file:.*|env_file:\\n      - ./${params.branch_name}/local.env|" docker-compose.yml;

                """
            }
        }
    }
}