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

                    # Crear el archivo local.env
                    cat > local.env <<EOL
                    DATABASE_NAME=${params.branch_name}_db
                    DATABASE_USER=user
                    DATABASE_PASSWORD=root
                    DATABASE_HOST=mysql
                    DATABASE_PORT=3306
                    EOL
                    """
            }
        }
        stage('PROYECT ENTRYPOINT') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cd /root/test/docker-tests/${params.branch_name};

                    # Crear el archivo entrypoint.sh
                    cat > entrypoint.sh <<EOL
                    #!/bin/bash

                    while ! nc -z $DATABASE_HOST $DATABASE_PORT; do
                    sleep 1
                    done

                    python manage.py makemigrations
                    python manage.py migrate

                    exec "$@"
                    EOL

                    """
            }
        }
        stage('PROYECT DOCKERFILE') {
            steps {
                sshCommand remote: remote,
                    command: """
                    cd /root/test/docker-tests/${params.branch_name};

                    # Crear el archivo Dockerfile
                    cat > Dockerfile <<EOL
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
                    # CMD ["uvicorn", "django_settings.asgi:app", "--reload", "--host", "0.0.0.0", "--port", "8050"]
                    CMD ["python", "manage.py", "runserver", "0.0.0.0:8050"]
                    EOL

                    """
            }
        }

    }
}
