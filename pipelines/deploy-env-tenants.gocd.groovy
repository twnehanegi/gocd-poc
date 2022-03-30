def allEnvs = ['poc'];

GoCD.script {
    pipelines {
        allEnvs.each {  env ->
            pipeline("$env-environment") {
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
                    }
                    dependency('build') {
                        pipeline = "Test-pipeline"
                        stage = 'Test-stage'
                        name = 'material1'
                    }
                    dependency('build') {
                        pipeline = "NewTest-pipeline"
                        stage = 'Test-stage'
                        name = 'material2'
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
                                        commandLine = ["echo", "$environment"]
                                    }
                                    if (environment == 'DEV') {
                                        fetchArtifact{
                                            pipeline = 'Test-pipeline'
                                            stage = 'Test-stage'
                                            runIf = 'any'
                                            job = 'Test-job'
                                            source = 'cruise-output/console.log'
                                            artifactOrigin = 'gocd'
                                            isFile = true
                                        }
                                    }
                                    if (environment == 'RELEASE') {
                                        fetchArtifact{
                                            pipeline = 'NewTest-pipeline'
                                            stage = 'Test-stage'
                                            runIf = 'any'
                                            job = 'Test-job'
                                            source = 'cruise-output/console.log'
                                            artifactOrigin = 'gocd'
                                            isFile = true
                                        }
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
