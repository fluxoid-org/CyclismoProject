#!/bin/bash

JFORMICA_DIR=~/code/CyclismoProject/Cyclismo/libs/jformica
FLUXUTILS_DIR=~/code/CyclismoProject/Cyclismo/libs/FluxUtils

cp "$JFORMICA_DIR"/jformica_core/build/libs/*.jar .
cp "$JFORMICA_DIR"/jformica_jsr80/build/libs/*.jar .
cp "$FLUXUTILS_DIR"/build/libs/*.jar .
