pipeline {
    agent any

    stages {
        stage("simple_test") {
            steps{
                 catchError {
                build(job: "simple_test")
                }
            }
        }
    }
}