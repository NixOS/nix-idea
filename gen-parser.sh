#!/bin/sh
src=`pwd`
java -cp $src/vendor/idea-IC/lib:$src/vendor/GrammarKit/lib \
  -jar $src/vendor/GrammarKit/lib/grammar-kit.jar \
  $src/src/gen/java \
  $src/src/main/lang/Nix.bnf
