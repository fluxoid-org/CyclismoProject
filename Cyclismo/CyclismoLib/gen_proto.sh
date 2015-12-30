#!/bin/sh
# This script (re)generates the sensor protocol buffers class. You should
# run this if sensor.proto is modified. It will overwrite any existing Sensor
# class. Make sure that the version of protoc is the same as the dependency in
# 'build.gradle'.
SRC_DIR=./proto
exec protoc -I=$SRC_DIR $SRC_DIR/sensor.proto --java_out=./src
