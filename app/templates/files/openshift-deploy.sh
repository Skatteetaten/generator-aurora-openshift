#!/bin/bash
INFO=$'\033[0;33m'
CLEAR=$'\033[0m'

header() {
  printf "\n\n$INFO%s\n------------\n$CLEAR" "$*"
}
oc whoami -t &> /dev/null || echo "Log into oc and ao"

yoRc=$(jq '.["generator-aurora-openshift"]' .yo-rc.json)

project=$(echo $yoRc | jq ".namespace" -r)
name=$(echo $yoRc | jq ".baseName" -r)
affiliation=$(echo $yoRc | jq ".affiliation" -r)

namespace=$affiliation-$project

oc get bc "$name" -n $namespace &> /dev/null || (
 header "Deploy the application with ao"
 ao deploy $project/$name

 header "The following openshift objects are made in $namespace"
 oc get all -l app=${name} -n ${namespace}
)


header "Package the application with mvn"
mvn package

leveransepakke=$(find target -type f -name "*-Leveransepakke.zip")

header "Start OpenShift binary build on OpenShift"
oc start-build $name --from-file=$leveransepakke --follow --wait -n $namespace

which stern &> /dev/null || exit 0

header "Deploy started since ImageStream listen to $name:latest"

after_close() {
 url="http://$name-$namespace.utv.paas.skead.no/api/counter"
 header "URL for application is"
 echo "$url"
 header "Calling the counter endpoint twice"
 http "$url"
 http "$url"
}

trap after_close INT

stern ${name} -n ${namespace}
