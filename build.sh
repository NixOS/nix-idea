#!/bin/sh -e

mvn clean install:install-file dependency:unpack install assembly:single && mvn proguard:proguard
