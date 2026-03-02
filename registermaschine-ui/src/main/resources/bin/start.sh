#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
"$DIR/jre/bin/java" --enable-native-access=ALL-UNNAMED -jar "$DIR/app.jar"