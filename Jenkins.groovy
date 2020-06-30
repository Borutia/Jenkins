
pipeline {
    agent{node('master')}
    stages {
        stage('Clean workspace & dowload dist') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop isng"
                            sh "echo '${password}' | sudo -S docker container rm isng"
                        } catch (Exception e) {
                            print 'Container not exist'
                        }
                    }
                }
                script {
                    echo 'Update from repository'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'Dmitry_Bor', url: 'https://github.com/Borutia/Jenkins.git']]])
                }
            }
        }
        stage ('Build & run docker image'){
            steps{
                script{
                     withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {

                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t dmitry_bor"
                        sh "echo '${password}' | sudo -S docker run -d -p 0001:80 --name isng -v /home/adminci/is_mount_dir:/stat dmitry_bor"
                    }
                }
            }
        }
        stage ('Get stats & write to file'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        
                        sh "echo '${password}' | sudo -S docker exec -t isng bash -c 'df -h > /stat/statistics.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t isng bash -c 'top -n 1 -b >> /stat/statistics.txt'"
                    }
                }
            }
        }
        
    }

    
}
