#!/bin/bash
INFO=$'\033[0;33m'
CLEAR=$'\033[0m'

header() {
  printf "$INFO%s\n$CLEAR" "$*"
}
oc whoami -t &> /dev/null || echo "Log into oc and ao"

yoRc=$(jq '.["generator-aurora-openshift"]' .yo-rc.json)

project=$(echo $yoRc | jq ".namespace" -r)
name=$(echo $yoRc | jq ".baseName" -r)
affiliation=$(echo $yoRc | jq ".affiliation" -r)

namespace=$affiliation-$project

oc get bc "$name" -n $namespace &> /dev/null || (
 header "AO deploy" && ao deploy $project/$name)

header "Mvn package"
mvn package

leveransepakke=$(find target -type f -name "*-Leveransepakke.zip")

header "Start OpenShift binary build on OpenShift"
oc start-build $name --from-file=$leveransepakke --follow --wait -n $namespace

which stern &> /dev/null || exit 0

header "Tail development $name"

after_close() {
 url="http://$name-$namespace.utv.paas.skead.no/api/counter"
 header "URL for application is"
 echo "$url"
}

trap after_close INT

stern ${name} -n ${namespace}
