#!/bin/bash

unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     machine=Linux;;
    Darwin*)    machine=Mac;;
    CYGWIN*)    machine=Cygwin;;
    MINGW*)     machine=MinGw;;
    *)          machine="UNKNOWN:${unameOut}"
esac
echo ${machine}
# JAVA_VERSION=`java -version 2>&1 | head -n 1 | cut -d\" -f 2`

if  [ "$machine" = "Linux" ]; then
	xterm -hold -e "java -jar SynBioHub-CLI.jar"
elif [ "$machine" = "Mac" ]; then
	osascript -e 'do shell script "java -jar SynBioHub-CLI.jar"'
elif [ "$machine" = "MinGw" ]; then
	mintty -h always "java -jar SynBioHub-CLI.jar"
fi
