#!/bin/bash

# requirments:
# java 8

# Paths to all important folders
TARGET_FOLDER="$(realpath $1)"
SCRIPT_DIR="$(dirname "$(realpath "$0")")"
SOOT_PATH="$(realpath $SCRIPT_DIR/ressources/soot-4.6.0-jar-with-dependencies.jar)"
OUTPUT_PATH="$(realpath $SCRIPT_DIR/output)"
SRC_PATH="$(realpath $SCRIPT_DIR/src/create_mapper.java)"
BUILD_PATH="$(realpath $SCRIPT_DIR/build)"

total_jar_files=$(find "$TARGET_FOLDER" -type f -name "*.jar" | wc -l)
echo "Total .jar files to process: $total_jar_files"

#cleanup
rm -rf "$OUTPUT_PATH"/* "$OUTPUT_PATH"/.[!.]* "$OUTPUT_PATH"/..?
rm -rf "$BUILD_PATH"/* "$BUILD_PATH"/.[!.]* "$BUILD_PATH"/..?

# pre-build
javac -d build $SRC_PATH

echo "Create output_java.csv"
cd "./ByteCodeInfos"
./gradlew -Dorg.gradle.java.home=/usr/lib/jvm/java-1.8.0-openjdk-amd64/ run -PmainClass=ByteCodeInfos --args="-d="$TARGET_FOLDER" -o="$OUTPUT_PATH/mapper_java.csv""
cd ../

echo "create jimple files"
processed_jar_files=0
find "$TARGET_FOLDER" -type f -name "*.jar" | while read -r JAR_FILE; do
	((processed_jar_files++))
  echo "Progress: $processed_jar_files of $total_jar_files files processed"

	dir=$(dirname "$JAR_FILE")
  cd "$dir" || exit         
	jar -xf $JAR_FILE
	rm -rf META-INF
	jar_name=$(basename "$JAR_FILE" .jar)
  find "$dir" -type d -name "_*" | while read -r subdir; do
		find "$subdir" -type f -name "*.java" -exec rm -f {} \;
		java -cp $SOOT_PATH soot.Main -cp . -pp -process-dir $subdir -f J -output-format jimple > /dev/null 2>&1
		rm -rf "$subdir/StubClass"

		find "$subdir" -type f -name "*.class" | while read -r classfiles; do
			filename_without_extension=$(basename "$classfiles" .class)
			mv "./sootOutput/$filename_without_extension.jimple" "./${jar_name}_${filename_without_extension}.jimple"
		done 
		rm -rf $subdir
  done
	rm -rf sootOutput
	rm $JAR_FILE
	rm $jar_name.java
done

echo "create mapper.csv"
java -cp build create_mapper "$TARGET_FOLDER" "$OUTPUT_PATH/mapper_java.csv" "$OUTPUT_PATH/mapper.csv"