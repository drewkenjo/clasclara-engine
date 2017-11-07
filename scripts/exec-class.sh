#!/bin/sh


if [ -n "${JAVA_HOME}" ]; then
    java_bin="${JAVA_HOME}/bin/java"
else
    java_bin=java
fi

if [ "$1" = "-t" ]; then
    shift
    cache_file=./build/tmp/test_classpath
else
    cache_file=./build/tmp/classpath
fi

class_path=$(cat "${cache_file}")


exec "${java_bin}" -cp "${class_path}" "$@"
