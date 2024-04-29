# NixIDEA - A Nix language plugin for IntelliJ IDEA

[![Build Status](https://github.com/NixOS/nix-idea/actions/workflows/build.yml/badge.svg?branch=master)][build-status]
[![Version](https://img.shields.io/jetbrains/plugin/v/nix-idea)][marketplace]

<!-- Plugin description -->

This plugin has the goal of being generally useful when working with nixpkgs/NixOS/nixops.
It currently adds support for the Nix language.
The following features are available:

 *  Syntax Highlighting
 *  Real time detection of syntax errors

We would also like to provide additional support for Nix/NixOS/NixOps,
but the following features are currently **not implemented**:

 *  Linting, code completion, formatting
 *  Profile management
 *  Run configurations
 *  Templates for common usage patterns

<!-- Plugin description end -->

## Install

### From JetBrains marketplace

The plugin can be found at the Jetbrains plugin repository as
[NixIDEA][marketplace].

 *  Goto **File > Settings > Plugins > Marketplace**
 *  Type **NixIDEA** into the search bar
 *  Click **Install**

### From ZIP file

You can also install the plugin from a ZIP file.

 *  Goto **File > Settings > Plugins**
 *  Click onto the **wheel icon** on the top
 *  Choose **Install Plugin from Disk**

You can find corresponding ZIP files [on GitHub][releases] or build them
yourself as described below.

## Build

### Build preparation

Follow the following steps before you build the project the first time.

 *  Clone the repository
 *  Ensure that you have a JDK for Java 11 or higher on your PATH
 *  Only on NixOS: Setup JetBrains Runtime (JBR) from `<nixpkgs>`
    ```sh
    nix-build '<nixpkgs>' -A jetbrains.jdk -o jbr
    ```

### Build execution

After you have completed the preparation, you can build the plugin by
running the `build` task in Gradle.

```sh
./gradlew build
```

You should then find the plugin at
`build/distributions/NixIDEA-<version>.zip`.

## Credits

The *Nix Snowflake* (logo of NixOS) is designed by Tim Cuthbertson (@timbertson).
It is made available under [CC-BY license](https://creativecommons.org/licenses/by/4.0/)
at [NixOS/nixos-artwork](https://github.com/NixOS/nixos-artwork/tree/master/logo).
The variants used by this plugin may have been modified to comply with JetBrains' Guidelines.

[build-status]:
<https://github.com/NixOS/nix-idea/actions/workflows/build.yml?query=branch%3Amaster>
"Latest builds at GitHub Actions"
[marketplace]:
<https://plugins.jetbrains.com/plugin/8607-nixidea/>
"NixIDEA on JetBrains Marketplace"
[releases]:
<https://github.com/NixOS/nix-idea/releases>
"Releases · NixOS/nix-idea"
