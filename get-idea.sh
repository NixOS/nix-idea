#!/bin/sh -e

# This is needed since we want to strip the buildnr..
# maven-dependency-plugin cannot strip leading directory from archives.

pname="ideaIC"
pversion="2016.2.3"
name=$pname-$pversion
tar=$name.tar.gz

[ ! -d vendor ] && mkdir vendor
[ ! -f $tar ] && curl -kOL https://download.jetbrains.com/idea/${tar}
tar xzf $tar -C vendor/.
cd vendor
mv idea-IC* idea-IC
zip -r idea-IC.zip idea-IC
cd -
