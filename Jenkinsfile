#!/usr/bin/env groovy

def scriptVersion = 'v3.1.0'
def jenkinsfile
fileLoader.withGit('https://git.aurora.skead.no/scm/ao/aurora-pipeline-scripts.git', scriptVersion) {
   jenkinsfile = fileLoader.load('templates/web-bibliotek')
}

jenkinsfile.run(scriptVersion)