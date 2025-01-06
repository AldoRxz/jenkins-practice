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
        stage('SET CRED'){
            steps {
                script{
                    remote.user = 'root'
                    remote.password = "${CRED_PSW}"
                    remote.allowAnyHosts = true
                }
            }
        }
        stage('MYSQL BUILD') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cd /root/test/docker-tests/mysql;

                    # Crear el archivo local.env
                    cat > local.env <<EOF
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=${params.branch_name}_db
MYSQL_USER=user
MYSQL_PASSWORD=root
EOF
                """
            }
        }

        stage('MYSQL INIT.SQL') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cd /root/test/docker-tests/mysql;

                    # Crear el archivo init.sql
                    cat > init.sql <<EOF
GRANT ALL PRIVILEGES ON test_${params.branch_name}_db.* TO 'user'@'%';
FLUSH PRIVILEGES;
EOF
                """
            }
        }

    }
}
