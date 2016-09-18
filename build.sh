#!/bin/sh -e

./gen-lexer.sh
# not working, something's wrong with gk
#./gen-parser.sh
mvn clean install:install-file dependency:unpack install assembly:single && mvn proguard:proguard
