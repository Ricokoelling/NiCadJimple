#!/bin/bash

# requirments:
# java 8

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
OUTPUT_PATH="$(realpath $SCRIPT_DIR/output)"
BUILD_PATH="$(realpath $SCRIPT_DIR/build)"

SRC_PATH="$(realpath $SCRIPT_DIR/src/CloneMapper.java)"
BCE_PATH="$(realpath $OUTPUT_PATH/output_bce.csv)"
MAP_PATH="$(realpath $OUTPUT_PATH/mapper.csv)"


javac -d build $SRC_PATH

java -cp build CloneMapper "$MAP_PATH" "$BCE_PATH" "$OUTPUT_PATH/output.csv"
