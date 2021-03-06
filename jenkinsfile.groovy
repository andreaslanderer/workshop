// Add this line ath the beginning of the Jenkinsfile
def artefactVersion;

podTemplate(label: "dotnet-31",
        cloud: "openshift",
        inheritFrom: "maven",
        containers: [
                containerTemplate(name: "jnlp",
                        image: "registry.redhat.io/dotnet/dotnet-31-jenkins-agent-rhel7:latest",
                        ttyEnabled: true,
                        envVars: [
                                envVar(key: "CONTAINER_HEAP_PERCENT", value: "0.25")
                        ])
        ],
        volumes: [
        ]) {
    // node("dotnet-31") {
    node() {
        stage("checkout") {
            sh 'printenv'
            checkout scm
        }

        // Add this after the checkout stage in the Jenkinsfile
        stage("gitversion") {
            // Ex.: ai_m_15-06-2019_08_22_9115204e

            String branchName = sh(returnStdout: true, script: "git rev-parse --abbrev-ref HEAD").trim()
            echo "Branch name: ${branchName}"

            String branchIndicator;
            if (branchName.equals("master")) {
                branchIndicator = 'm' // indicates master branch
            } else if (branchName.equals("develop")) {
                branchIndicator = 'd' // indicates master branch
            } else {
                branchIndicator = 'f' // indicates feature... actually just not master :-)
            }

            def today = new Date()
            String formattedDate = today.format('dd-MM-yyyy_HH_mm')
            String gitCommitSha = sh(returnStdout: true, script: "git rev-parse --short=7 HEAD").trim()
            echo "Git sha: ${gitCommitSha}"

            artefactVersion = "ai_${branchIndicator}_${formattedDate}_${gitCommitSha}" // adding date with time as 15-06-2019_08_22

            echo "Artifact identifier: ${artefactVersion}"
        }

        stage("dotnet publish") {
            // sh 'dotnet publish src/Zuehlke.OpenShiftDemo/Zuehlke.OpenShiftDemo.csproj -c Release -o ./artifacts/app/publish --no-restore --no-build /p:AssemblyVersion=${GitVersion_AssemblySemVer} /p:FileVersion=${GitVersion_AssemblySemFileVer} /p:InformationalVersion=${GitVersion_InformationalVersion}'
            zip zipFile: "demo-app-${artefactVersion}.zip", archive: true, dir: "./artifacts/app/publish", glob: "**/*.*"
        }
    }
}