def remote = [:]
remote.name = "${params.host_name}"
remote.host = "${params.host_ip}"
remote.branch = "${params.branch_name}"

pipeline {
    agent any

    environment {
        CRED = credentials('sandbox')
        DB = "jenkins_db"
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
        stage('DOCKER BUILD') {
            steps {
                sshCommand remote: remote,
                    command: "cd /root/test/docker-tests && docker compose up --build -d"
            }
        }
    }
}
