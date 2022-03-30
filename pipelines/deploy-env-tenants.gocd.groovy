def allEnvs = ['qa-tenant5',
        'qa12-tenant5'];

GoCD.script {
    pipelines {
        allEnvs.each {  env ->
            pipeline("$env") {
                def targetenv = env.split('-')[0]
                group = 'deploy'
                environmentVariables = [
                        'ENV': 'DEV',
                        'ENV_SIZE': 'small',
                        'CLIENT':'client1',
                        'BUILD_VERSION':'',
                        'SNAPSHOT_VERSION':'',
                        'TENANT_ID': 'tenant5',
                        'UPDATE_DATA_BAGS': 'true',
                        'BERKSFILE_PATH':'Berksfile',
                        'CHEF_RUBY':'/opt/chef/embedded/bin',
                        'BERKS_UPDATE':'false',
                        'CHEF_ENV': targetenv
                ]
                def environment = environmentVariables.ENV
                materials {
                    git {
                        branch = 'main'
                        url = 'https://github.com/twnehanegi/gocd-poc.git'
                        blacklist = ['**/*']
                        name = 'material0-'+ environment
                        destination = main
                    }
                    git {
                        branch = 'branch2'
                        url = 'https://github.com/twnehanegi/gocd-poc.git'
                        blacklist = ['**/*']
                        name = 'material1-'+ environment
                        destination = branch2
                    }
                }
                stages {
                    stage('deploy') {
                        approval { type = 'manual' }
                        fetchMaterials = true
                        artifactCleanupProhibited = true
                        cleanWorkingDir = false
                        jobs {
                            job('deploy') {
                                tasks {
                                    exec {
                                        runIf = 'passed'
                                        commandLine = ["/bin/sh", "echo \"Your current working directory is:\""]
                                    }
                                }
                                runInstanceCount = null
                                timeout = 60
                            }
                        }
                    }
                }

            }

        }
    }
}
