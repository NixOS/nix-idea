# NixIDEA - A Nix language plugin for Intellij IDEA.

[![Build Status](https://travis-ci.org/NixOS/nix-idea.svg?branch=master)](https://travis-ci.org/NixOS/nix-idea)

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

You should then find the plugin in `build/distributions/nix-idea-<version>.zip`.

### Import it

In `Settings -> Plugin -> <little wheel icon> -> from local disk...`

## From a packaged release

### Jetbrains marketplace

The plugin can be found at the Jetbrains plugin repository as
[Nix-IDEA](https://plugins.jetbrains.com/plugin/8607).

### Prebuild release

Same process as locally to import it, just skip the build part of it.
