#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
which npm || (
  echo "Installing node" &&  \
  sudo apt-get install npm nodejs-legacy &&  \
  sudo npm install -g yo \
  sudo chown -R $(whoami) $(sudo npm config get prefix)/{lib/node_modules,bin,share})

which yo || sudo npm install -g yo

sudo npm install -g $DIR


