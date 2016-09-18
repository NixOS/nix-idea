#!/bin/sh

java -jar vendor/jflex-1.7.0-SNAPSHOT.jar \
  --skel src/main/lang/idea-flex.skeleton \
  --nobak src/main/lang/Nix.flex \
  -d src/gen/java/org/nixos/idea/lang
