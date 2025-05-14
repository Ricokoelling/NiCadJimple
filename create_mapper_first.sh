#!/bin/bash

# requirments:
# java 8

# Paths to all important folders
TARGET_FOLDER="$(realpath $1)"
SCRIPT_DIR="$(dirname "$(realpath "$0")")"
OUTPUT_PATH="$(realpath $SCRIPT_DIR/output)"
BUILD_PATH="$(realpath $SCRIPT_DIR/build)"

#cleanup
rm -rf "$OUTPUT_PATH"/* "$OUTPUT_PATH"/.[!.]* "$OUTPUT_PATH"/..?
rm -rf "$BUILD_PATH"/* "$BUILD_PATH"/.[!.]* "$BUILD_PATH"/..?

echo "Create output_java.csv"
cd "./ByteCodeInfos"
./gradlew -Dorg.gradle.java.home=/usr/lib/jvm/java-1.8.0-openjdk-amd64/ run -PmainClass=ByteCodeInfos --args="-d="$TARGET_FOLDER" -t=8  -o="$OUTPUT_PATH/mapper_java.csv" "
cd ../