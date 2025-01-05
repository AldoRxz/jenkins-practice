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
        stage('PROYECT BUILD') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cd /root/test/docker-tests/${params.branch_name};

                    sed -i 's/^DATABASE_NAME=.*/DATABASE_NAME=${params.branch_name}_db/' local.env;

                    """
            }
        }

    }
}
