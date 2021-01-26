/* stagesController.groovy
   ##################################################
   # Created by Lin Ru at 2018.10.01 22:00          #
   #                                                #
   # A Part of the Project jenkins-library          #
   #  https://github.com/Statemood/jenkins-library  #
   ##################################################
*/

import me.rulin.ci.Git
import me.rulin.ci.Language
import me.rulin.ci.SonarQube
import me.rulin.docker.Docker
import me.rulin.k8s.Kubernetes

def preProcess() {
    stage("Pre-Process") {
        // Set default info
        // Set build info
        // Check parameters

        log.i "Stage Pre-Process OK"
    }
}

def gitClone() {
    stage("Git Clone") {
        try {
            def private revision = Config.data['revision']
            def private     repo = Config.data['repo']

            log.i "Git clone " + revision + " " + repo

            git credentialsId: Config.data['credentials.id'],
                branch: revision,
                url: repo

            Config.data['commit.id'] = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

            return
        } catch (e) {
            log.e "Ops! Error occurred during git checkout"
            throw e
        }
    }
}

def sonar() {
    stage("SonarQube Scanner") {
        def sonar = new SonarQube()
        sonar.scanner()
    }
}

def compile() {
    stage("Build Code") {
        def language = new Language()
        language.seletor(Config.data['language'])
    }
}

def test() {
    def private utc = Config.data['build.command.unit.test']
    if (utc) {
        stage("Unit Test") {
            log.i "Test by command: " + utc

            sh(utc)
        }
    }
}

def docker(){
    node(STAGE_DOCKER) {
        def private  docker = new Docker()
        
        def private  tag = GIT_REVISION    + '-' + Config.data['commit.id'][0..8]
        env.DOCKER_IMAGE = DOCKER_REGISTRY + '/' + PROJECT_NAME + '/' + APP_NAME + ':' + tag 

        docker.genDockerfile()
        docker.build(DOCKER_IMAGE)
        docker.login()
        docker.push(DOCKER_IMAGE)
    }
}

def kubernetes(){
    def k8s = new Kubernetes()

    k8s.updateYaml()
}

return this