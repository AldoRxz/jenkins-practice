def remote = [:]
remote.name = "${params.host_name}"
remote.host = "${params.host_ip}"
remote.branch = "${params.branch_name}"
remote.branchid = "${params.branch_id}"

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
        stage('CREATE PROYECT FOLDER') {
            steps {
                sshCommand remote: remote,
                    command: "cd /root/test/docker-tests && mkdir -p ${params.branch_id}"
            }
        }
        stage('CLONE REPO') {
            steps {
                sshCommand remote: remote,
                    command: "cd /root/test/docker-tests/${params.branch_id} && git clone https://github.com/CodeNation-Studio-Dev/keski_proj_backend.git ${params.branch_name}"
            }
        }
        stage('CHECKOUT BRANCH') {
            steps {
                sshCommand remote: remote,
                    command: "cd /root/test/docker-tests/${params.branch_id}/${params.branch_id} && git stash && git fetch && git checkout ${params.branch_name} && git pull"
            }
        }
    }
}
