#!/bin/bash

# Resolve script directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$SCRIPT_DIR/../backupcat4j.jar"
FFMPEG="$SCRIPT_DIR/../ffmpeg/ffmpeg"
FFPROBE="$SCRIPT_DIR/../ffmpeg/ffprobe"

# Sample source/target - update as needed
SOURCE="$HOME/media"
TARGET="$HOME/backup"

# Run the backup JAR
java -jar "$JAR" \
source="$SOURCE" \
target="$TARGET" \
ffmpeg="$FFMPEG" \
ffprobe="$FFPROBE" \
organize=FULL \
--compress \
--resume \
--replace \
--bitrate=3000000