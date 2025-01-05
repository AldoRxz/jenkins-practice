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

                    sed -i 's/^MYSQL_DATABASE=.*/MYSQL_DATABASE=${params.branch_name}_db/' local.env;

                    sed -i "s/ON .*/ON test_${params.branch_name}_db.* TO 'user'@'%';/" init.sql;

                    """
            }
        }

    }
}
