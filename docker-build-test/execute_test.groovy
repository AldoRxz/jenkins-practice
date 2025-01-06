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

        stage('EXECUTE DOCKER-TESTS') {
            steps {
                sshCommand remote: remote, 
                    command: "cd /root/test/docker-tests && docker exec api_${params.branch_name} python manage.py test api_app.tests.simple_test "
            }
        }
    }
}
