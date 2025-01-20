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
        stage('CREATE FOLDER') {
            steps {
                sshCommand remote: remote,
                    command: "cd /root/test/docker-tests && mkdir ${params.branch_name}"
            }
        }
        stage('CLONE REPO') {
            steps {
                sshCommand remote: remote,
                    command: "cd /root/test/docker-tests/${params.branch_name} && git clone https://github.com/CodeNation-Studio-Dev/keski_proj_backend.git ${params.branch_name}"
            }
        }
        stage('CHECKOUT BRANCH') {
            steps {
                sshCommand remote: remote,
                    command: "cd /root/test/docker-tests/${params.branch name} && git stash && git fetch && git checkout ${params.branch_name} && git pull"
            }
        }
    }
}
