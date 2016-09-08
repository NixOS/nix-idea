#!/bin/sh -e

# This is needed since we want to strip the buildnr..
# maven-dependency-plugin cannot strip leading directory from archives.

pname="ideaIC"
pversion="2016.2.3"
name=$pname-$pversion
tar=$name.tar.gz

[ ! -f $tar ] && wget --no-check-certificate https://download.jetbrains.com/idea/${tar}
tar xzf $tar -C vendor/.
cd vendor
zip -r idea-IC.zip idea-IC
cd -
