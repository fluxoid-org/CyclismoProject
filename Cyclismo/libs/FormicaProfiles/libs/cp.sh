#!/bin/bash

JFORMICA_DIR=~/code/CyclismoProject/Cyclismo/libs/jformica

cp "$JFORMICA_DIR"/jformica_core/build/libs/*.jar .
cp "$JFORMICA_DIR"/jformica_jsr80/build/libs/*.jar .

rm ./*-sources.jar
rm ./*-javadoc.jar
