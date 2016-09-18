#!/bin/sh
#              https://github.com/JetBrains/Grammar-Kit/releases/download/1.2.0.1/light-psi-all.jar
if [ ! -f vendor/GrammarKit/lib/grammar-kit.jar ]; then
cd vendor
[ ! -f GrammarKit.zip ] && wget https://github.com/JetBrains/Grammar-Kit/releases/download/1.4.2/GrammarKit.zip
[ ! -d GrammarKit ] && 7z x GrammarKit.zip
cd -
fi
cd vendor/GrammarKit/lib
for jar in jdom.jar trove4j.jar extensions.jar picocontainer.jar junit.jar idea.jar openapi.jar util.jar
do
    ln -s ../../idea-IC/lib/$jar .
done

