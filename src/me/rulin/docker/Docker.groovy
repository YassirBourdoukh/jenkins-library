/* Docker.groovy
   ##################################################
   # Created by Lin Ru at 2018.10.01 22:00          #
   #                                                #
   # A Part of the Project jenkins-library          #
   #  https://github.com/Statemood/jenkins-library  #
   ##################################################
*/

package me.rulin.docker

def String cmd(String c){
    try {
        sh("sudo docker $c")
    }
    catch (e) {
        throw e
    }
}

def private genDockerfile(String f='Dockerfile', String t='.', String d=Config.data.base_web_root, String c=null){
    if (fileExists(Config.data.docker_ignore_file)) {
        log.i "Copy dockerignore file"

        sh('cp -rf' + Config.data.docker_ignore_file + ' .')
    }
    else {
        log.w 'File not found: ' + Config.data.docker_ignore_file
    }
    
    // Test Dockerfile exist
    check.file(f)
    def private dfc = []
    def private cid = Config.data.git_commit_id

    dfc.add("LABEL made.by=Jenkins job.name=$JOB_NAME build.user=$BUILD_USER commit.id=$cid")
    dfc.add("RUN mkdir -p $d")
    dfc.add("COPY $t $d")

    sh("echo >> $f")

    for(s in dfc) {
        sh("echo $s >> $f")
    }
}

def private build(String image_name) {
    check.file('Dockerfile')
    try {
        log.info "Build image: " + image_name

        timeout(time: DOCKER_IMAGE_BUILD_TIMEOUT, unit: 'SECONDS') {
            cmd("build $DOCKER_IMAGE_BUILD_OPTIONS -t $image_name .")
        }
    }
    catch (e) {
        println "Error occurred during build image"
        throw e
    }
}

def private push(String image_name){
    try {
        log.info "Push image " + image_name

        timeout(time: Config.data.docker_img_push_timeout, unit: 'SECONDS') {
            cmd("push $image_name")
        }
    }
    catch (e) {
        println "Error occurred during push image"
        throw e
    }
}

def login(String reg=DOCKER_REGISTRY, String opt=null){
    try {
        log.i "Login to Docker Registry " + reg

        timeout(time: Config.data.docker_login_timeout, unit: 'SECONDS') {
            withCredentials([
                usernamePassword(
                    credentialsId: Config.data.docker_account,
                    passwordVariable: 'registry_password',
                    usernameVariable: 'registry_username'
                )
            ]){
                cmd("login -u $registry_username -p $registry_password $reg")
            }
        }
    }
    catch (e) {
        println "Error occurred during push image"
        throw e
    }
}

def logout(){
    cmd("logout")
}