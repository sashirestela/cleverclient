#!/bin/bash

java_file="BasicExample"

if [ ! -z "$1" ]; then
  java_file="$1"
fi

main_class="io.github.sashirestela.cleverclient.example.${java_file}"

command="mvn -q exec:java -Dexec.mainClass=${main_class}"

echo $command

eval $command
