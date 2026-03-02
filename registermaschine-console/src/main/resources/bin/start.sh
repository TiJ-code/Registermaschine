#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
"$DIR/jre/bin/java" -jar "$DIR/app.jar"