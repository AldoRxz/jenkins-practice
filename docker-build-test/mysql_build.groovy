def remote = [:]
remote.name = "${params.host_name}"
remote.host = "${params.host_ip}"
remote.branch = "${params.branch_name}"
remote.branchid = "${params.branch_id}"

pipeline {
    agent any

    environment {
        CRED = credentials('sandbox')
    }

    stages {
        stage('SET CRED'){
            steps {
                script{
                    remote.user = 'root'
                    remote.password = "${CRED_PSW}"
                    remote.allowAnyHosts = true
                }
            }
        }
        stage('CREATE MYSQL FOLDER') {
            steps {
                sshCommand remote: remote,
                    command: "cd /root/test/docker-tests/${params.branch_name} && mkdir mysql"
            }
        }
        stage('MYSQL BUILD') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cd /root/test/docker-tests/${params.branch_name}/mysql;

                    # Crear el archivo local.env
                    cat > local.env <<EOF
MYSQL_ROOT_PASSWORD=CnsStd@422
MYSQL_DATABASE=${params.branch_name}_db
MYSQL_USER=user
MYSQL_PASSWORD=CnsStd@422
EOF
                """
            }
        }

        stage('MYSQL INIT.SQL') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cd /root/test/docker-tests/${params.branch_name}/mysql;

                    # Crear el archivo init.sql
                    cat > init.sql <<EOF
GRANT ALL PRIVILEGES ON `${params.branch_name}_db`.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON `test_${params.branch_name}_db`.* TO 'user'@'%';
FLUSH PRIVILEGES;
EOF
                """
            }
        }
        stage('MYSQL DOCKERFILE') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cd /root/test/docker-tests/${params.branch_name}/mysql;

                    # Crear el archivo init.sql
                    cat > Dockerfile <<EOF
FROM mysql:8.2.0

EXPOSE 3306

COPY init.sql /docker-entrypoint-initdb.d/

CMD ["mysqld"]

EOF
                """
            }
        }

    }
}
