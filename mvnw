#!/bin/sh
# Maven Wrapper script

set -e

# Detect OS
case "$(uname -s)" in
  CYGWIN*|MINGW*|MSYS*)
    MAVEN_CMD="mvn.cmd"
    ;;
  *)
    MAVEN_CMD="mvn"
    ;;
esac

# Check if Maven is installed
if command -v $MAVEN_CMD > /dev/null 2>&1; then
  exec $MAVEN_CMD "$@"
else
  echo "Maven not found. Please install Maven or use './mvnw install' with Maven Wrapper"
  exit 1
fi
