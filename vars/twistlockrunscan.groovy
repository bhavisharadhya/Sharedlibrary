def call(body)
{
    try {
        def TWISTLOCK_URL = env.TWISTLOCK_URL_PART ?: "https://gps-twistlock.ift.pearsondev.tech"

        sh """
            docker tag "${env.IMAGE_REPO}:latest" ${env.IMAGE_NAME} ${env.IMAGE_BRANCH}
            curl -s -k -L -u ${TWISTLOCK_CREDENTIALS_USR}:${TWISTLOCK_CREDENTIALS_PSW} ${TWISTLOCK_URL}/api/v1/util/twistcli > /tmp/twistcli && \
            chmod 0755 /tmp/twistcli && \
            /tmp/twistcli images scan \
            -u ${TWISTLOCK_CREDENTIALS_USR} \
            -p ${TWISTLOCK  _CREDENTIALS_PSW} \
            --address ${TWISTLOCK_URL} \
            --details \
            --output-file ./twistcli.log \
            --ci \
            ${env.IMAGE_NAME} | tee twistlockoutput
        """

        echo "BUILD_NUMBER = ${env.BUILD_NUMBER}"

        archiveArtifacts artifacts: 'twistcli.log'

        sh """
            if grep "Vulnerability threshold check results: PASS" twistlockoutput ; then
                echo "Scan succeeded"
                exit 0
            else
                echo "Scan failed due to vulnerability policy violations"
                exit 1
            fi
        """
    }

    catch(err) {
        println "[ERROR] : Error encountered while running twistlock image scan"
        throw (err)
    }
}
    
