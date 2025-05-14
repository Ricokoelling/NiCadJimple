#!/bin/bash

# requirments:
# java 8

# Paths to all important folders
TARGET_FOLDER="$(realpath $1)"
SCRIPT_DIR="$(dirname "$(realpath "$0")")"
OUTPUT_PATH="$(realpath $SCRIPT_DIR/output)"
SRC_PATH="$(realpath $SCRIPT_DIR/src/create_mapper.java)"

# pre-build
javac -d build $SRC_PATH

echo "create mapper.csv"
java -cp build create_mapper "$TARGET_FOLDER" "$OUTPUT_PATH/mapper_java.csv" "$OUTPUT_PATH/mapper.csv"
