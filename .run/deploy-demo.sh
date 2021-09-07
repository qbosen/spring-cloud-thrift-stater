#!/bin/bash
REMOTE_HOST=fedora
REMOTE_USER=root
REMOTE_HOME=/project/calculator
LOCAL_HOME_RELATE_PROJECT_HOME=thrift-starter-demo/calculator

DEBUG=0
SCRIPT_DIR="$(cd $(dirname "$0") && pwd -P)"
# 脚本位置的上级目录
PROJECT_HOME="$(dirname $SCRIPT_DIR)"

function upload() {
  # 相对 local_home 的文件路径
  local source="$PROJECT_HOME/$LOCAL_HOME_RELATE_PROJECT_HOME/$1"
  local dest="$REMOTE_HOME/$1"
  if [ -d "$source" ]; then
    ssh "$REMOTE_USER@$REMOTE_HOST" "mkdir -p $dest"
    scp -pr "$source" "$REMOTE_USER@$REMOTE_HOST:$(dirname $dest)"
  else
    ssh "$REMOTE_USER@$REMOTE_HOST" "mkdir -p $(dirname $dest)"
    scp -p "$source" "$REMOTE_USER@$REMOTE_HOST:$dest"
  fi

}
if [ $DEBUG != 0 ]; then
  set -x
fi

case "$1" in
build)
  cd "$PROJECT_HOME" || exit 1
  ./gradlew :thrift-starter-demo:calculator:calculator-client:bootJar -xtest
  ./gradlew :thrift-starter-demo:calculator:calculator-server:bootJar -xtest
  ./gradlew :thrift-starter-demo:calculator:calculator-pure-server:bootJar -xtest
  ;;
esac


upload docker-compose.yml
upload calculator-client/build/libs/
upload calculator-client/Dockerfile
upload calculator-server/build/libs/
upload calculator-server/Dockerfile
upload calculator-pure-server/build/libs/
upload calculator-pure-server/Dockerfile

{ set +x; } 2>/dev/null
