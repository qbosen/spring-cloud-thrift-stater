#!/bin/bash
REMOTE_HOST=fedora
REMOTE_USER=root
REMOTE_HOME=/project/calculator
LOCAL_HOME_RELATE_PROJECT_HOME=thrift-starter-demo/calculator

SCRIPT_DIR="$(cd $(dirname "$0") && pwd -P)"
# 脚本位置的上级目录
PROJECT_HOME="$(dirname $SCRIPT_DIR)"
DEBUG=0

function upload() {
  # 相对 local_home 的文件路径
  ssh "$REMOTE_USER@$REMOTE_HOST" "mkdir -p $(dirname $REMOTE_HOME/$1)"
  scp -pr "$1" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_HOME/$1"
}
if [ $DEBUG != 0 ]; then
  set -x
fi

case "$1" in
build)
  cd "$PROJECT_HOME"
  ./gradlew :thrift-starter-demo:calculator:calculator-client:bootJar -xtest
  ./gradlew :thrift-starter-demo:calculator:calculator-server:bootJar -xtest
  ./gradlew :thrift-starter-demo:calculator:calculator-pure-server:bootJar -xtest
  ;;
esac

cd "$PROJECT_HOME/$LOCAL_HOME_RELATE_PROJECT_HOME"

upload docker-compose.yml
upload calculator-client/build/libs
upload calculator-client/Dockerfile
upload calculator-server/build/libs
upload calculator-server/Dockerfile
upload calculator-pure-server/build/libs
upload calculator-pure-server/Dockerfile

{ set +x; } 2>/dev/null
