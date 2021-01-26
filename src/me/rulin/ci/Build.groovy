/* Language.groovy
   ##################################################
   # Created by Lin Ru at 2019.08.03 23:30          #
   #                                                #
   # A Part of the Project jenkins-library          #
   #  https://github.com/Statemood/jenkins-library  #
   ##################################################
*/

package me.rulin.ci

def build(String b_node='master', String b_file=null){
    try {
        node(b_node) {
            log.i "Preparing to build " + Config.data['language'].toUpperCase() + " project at node " + b_node + "."

            private  bc = Config.data['build_command']
            private  bo = Config.data['build_options']
            private cmd = bc + " " + bo

            log.i "test 21"
            try {
                log.i "test 22, bf=" + b_file + "."
                if (b_file){
                    log.i "test 23"
                    check.file(b_file)

                    log.i "Build with command: " + bc + ", options: " + bo
                    sh(cmd)
                }
            }
            catch (e) {
                log.i "test 24"
                throw e
            }
        }
    }
    catch (e) {
        log.e "An ERROR occurred during dispatch to node " + b_node
        throw e
    }
}