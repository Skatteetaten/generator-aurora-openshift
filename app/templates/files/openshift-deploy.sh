#!/bin/bash

oc whoami -t &> /dev/null || echo "Log into oc and ao"

yoRc=$(jq '.["generator-aurora-openshift"]' .yo-rc.json)

project=$(echo $yoRc | jq ".namespace" -r)
name=$(echo $yoRc | jq ".baseName" -r)
affiliation=$(echo $yoRc | jq ".affiliation" -r)

namespace=$affiliation-$project

oc get bc "$name" -n $namespace &> /dev/null || ao deploy $project/$name

echo "Mvn package"
mvn package

leveransepakke=$(find target -type f -name "*-Leveransepakke.zip")

echo "Start OpenShift binary build"
oc start-build $name --from-file=$leveransepakke --follow --wait -n $namespace

which stern &> /dev/null || exit 0

echo "Tail logs with stern $name"

after_close() {
  xdg-open "http://$name-$namespace.utv.paas.skead.no/api/counter"
}

trap after_close INT

stern $name -n $namespace