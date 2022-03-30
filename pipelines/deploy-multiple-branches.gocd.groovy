
GoCD.script {
    branches {
        matching {
            url = "git@github.com:twnehanegi/gocd-poc.git"
            pattern = ~/^refs\/heads\/.+/

            onMatch { ctx ->
                println("hey!")
                pipeline("pr-${ctx.branchSanitized}") {
                    group = 'deploy'
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
}