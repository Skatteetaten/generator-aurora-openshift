#!/usr/bin/env bash

which node || sudo apt-get install nodejs npm -y

#stupid debian do not use node as executable
which node || sudo ln -s `which nodejs` /usr/bin/node

which npm || sudo apt-get install npm -y

which ~/.ao/bin/yo || npm install yo -g --prefix=~/.ao

#hvis vi publiserer på npm registry kan vi sikker få til å sjekke om vi er utdaterte her.
npm install -g  --prefix=~/.ao https://github.com/Skatteetaten/generator-aurora-openshift

#følgende må brukes for å installere
echo "~/.ao/bin/yo aurora-openshift <navn> for å lage en app"
