#!/bin/bash
error_exit() {
  echo "$1"
  exit 1
}

if [[ $# -ne 1 ]]; then

    error_exit "openshift-deploy <build config name>"
fi

oc get bc "$1" &> /dev/null || error_exit  "Build Config $1 does not exist"

echo "Mvn package"
mvn clean package

leveransepakke=$(find target -type f -name "*-Leveransepakke.zip")

echo "Start OpenShift binary build"
oc start-build $1 --from-file=$leveransepakke --follow --wait


which stern &> /dev/null && echo "Tail logs with stern $1" && stern $1
