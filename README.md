# NixIDEA - A Nix language plugin for Intellij IDEA.

[![Build Status](https://github.com/NixOS/nix-idea/actions/workflows/build.yml/badge.svg?branch=master)][build-status]
[![Version](https://img.shields.io/jetbrains/plugin/v/nix-idea)][marketplace]

<!-- Plugin description -->

This plugin has the goal of being generally useful when working with nixpkgs/NixOS/nixops, it aims
to provide the following:

* Syntax Highlighting
* Linting
* Profile management
* Suggestions for:
    * Attributes
    * Builtins
    * Filesystem paths
* Templates for common usage patterns

<!-- Plugin description end -->

# Install

## Manually from local sources

### Build it

Using the gradle wrapper is easy:

    ./gradlew build

You should then find the plugin in `build/distributions/NixIDEA-<version>.zip`.

### Import it

In `Settings -> Plugin -> <little wheel icon> -> from local disk...`

## From a packaged release

### Jetbrains marketplace

The plugin can be found at the Jetbrains plugin repository as
[NixIDEA][marketplace].

### Prebuild release

Same process as locally to import it, just skip the build part of it.


[build-status]:
<https://github.com/NixOS/nix-idea/actions/workflows/build.yml?query=branch%3Amaster>
"Latest builds at GitHub Actions"
[marketplace]:
<https://plugins.jetbrains.com/plugin/8607-nixidea/>
"NixIDEA on JetBrains Marketplace"
