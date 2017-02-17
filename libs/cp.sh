#!/bin/bash

JFORMICA_DIR=~/code/CyclismoProject/Cyclismo/libs/jformica
FLUXUTILS_DIR=~/code/CyclismoProject/Cyclismo/libs/FluxUtils
JTURBO_DIR=~/code/CyclismoProject/Cyclismo/libs/JTurbo

cp "$JFORMICA_DIR"/jformica_core/build/libs/*.jar .
cp "$JFORMICA_DIR"/jformica_jsr80/build/libs/*.jar .
cp "$FLUXUTILS_DIR"/build/libs/*.jar .
cp "$JTURBO_DIR"/build/libs/*.jar .
rm ./*-sources.jar
rm ./*-javadoc.jar
