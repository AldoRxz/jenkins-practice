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
        stage('PROYECT BUILD') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cd /root/test/docker-tests/${params.branch_name}/${params.branch_name};

                    python3 /root/automation/scripts/update_settings_file.py -t "do" -e "dev" -p "/root/test/docker-tests/${params.branch_name}/${params.branch_name}" -d "${params.branch_name}_db" -u "user" --local_host "mysql"

                    """
            }
        }
        stage('PROYECT ENTRYPOINT') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cp -p /root/test/docker-tests/config/entrypoint.sh /root/test/docker-tests/${params.branch_name}/${params.branch_name}
                    """
            }
        }
        stage('PROYECT DOCKERFILE') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cd /root/test/docker-tests/${params.branch_name}/${params.branch_name};

                    # Crear el archivo Dockerfile
                    cat > dev.Dockerfile <<EOL
FROM python:3.10-slim

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y \
    pkg-config \
    libmariadb-dev \
    gcc \
    netcat-openbsd \
    && apt-get clean

RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8050

ENTRYPOINT ["/app/entrypoint.sh"]
CMD ["python", "manage.py", "runserver", "0.0.0.0:8050",  "--settings=superapp.settings_dev"]
EOL
                    """
            }
        }

    }
}
