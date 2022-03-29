import cd.go.contrib.plugins.configrepo.groovy.dsl.GoCD


def releases = ['18.1.0', '17.12.0']

GoCD.script {
    branches {
        matching {
            url = "https://github.com/marques-work/sample-node"
            pattern = ~/^refs\/heads\/.+/

            onMatch { ctx ->
                println("hey!")
                pipeline("pr-${ctx.branchSanitized}") {
                    materials { add(ctx.repo) }
                    stages {
                        stage('tests') {
                            jobs {
                                job('units') {
                                    tasks { bash { commandString = 'whoami' } }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pipelines {
        releases.each { releaseNumber ->
            pipeline("website-${releaseNumber}") {
                group = "example-group"

                trackingTool {
                    link = 'https://github.com/gocd/api.go.cd/issues/${ID}'
                    regex = ~/##(\\d+)/
                }

                materials {
                    git {
                        url = 'https://github.com/gocd/api.go.cd'
                        branch = "release-${releaseNumber}"
                    }
                }
                stages {
                    stage('build-website') {
                        jobs {
                            job('build') {
                                tasks {
                                    bash {
                                        commandString = 'bundle install --path .bundle -j4'
                                    }
                                    bash {
                                        commandString = 'bundle exec rake build'
                                    }
                                }

                                artifacts {
                                    build {
                                        source = "build/${releaseNumber}"
                                        destination = 'website'
                                    }
                                }

                                tabs {
                                    tab('website') { path = "website/${releaseNumber}/index.html" }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}