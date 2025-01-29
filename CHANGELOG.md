# Changelog

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [0.4.0.17] - 2025-01-29

### Added

- Experimental support for resolving variables.
  The feature is disabled by default since the functionality is rather limited for now.
  Feel free to comment your feedback at [issue #87](https://github.com/NixOS/nix-idea/issues/87).
- Support for resolving simple path references
- Support for simple spell checking
- Automatic insertion of closing quotes and braces
- Support for *Code | Move Element Left/Right* (<kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>Shift</kbd>+<kbd>←/→</kbd>)
- Support for IDEA 2025.1

## [0.4.0.16] - 2024-09-22

### Added

- Support for IDEA 2024.3

### Removed

- Support for IDEA 2023.3

## [0.4.0.15] - 2024-08-14

### Added

- Support for IDEA 2024.2

### Removed

- Support for IDEA 2023.2

## [0.4.0.14] - 2024-06-10

### Added

- Support for code formatting via external commands ([#80](https://github.com/NixOS/nix-idea/pull/80))

## [0.4.0.13] - 2024-04-29

### Added

- Plugin logo for easier recognition
- Experimental Language Server support using IDEA's LSP API (#68)  
  [(Only works for paid versions of IDEA 😞)](https://blog.jetbrains.com/platform/2023/07/lsp-for-plugin-developers/#supported-ides)

### Changed

- Icon of `*.nix` files is now using SVG format
- Icon of `*.nix` files got a separate variant for dark backgrounds
- Update Grammar-Kit: 2021.1.2 -> 2022.3.2
- Update JetBrains' fork of JFlex: 1.7.0-1 -> 1.9.2

### Removed

- Support for IDEA 2023.1

### Fixed

- Variables behind `inherit` keyword not correctly resolved during highlighting

## [0.4.0.12] - 2024-03-29

### Added

- Support for IDEA 2024.1

### Removed

- Support for IDEA 2022.3

## [0.4.0.11] - 2023-10-22

### Added

- Support for IDEA 2023.3

### Removed

- Support for IDEA 2022.2

## [0.4.0.10] - 2023-07-30

### Added

- Support for IDEA 2023.2

### Removed

- Support for IDEA 2022.1

### Fixed

- Final changes on release notes not applied to *“What’s New”* section
  of published plugin

## [0.4.0.9]

### Added

- Support for [string interpolation in paths](https://nixos.org/manual/nix/stable/language/string-interpolation#path) (#60)

## [0.4.0.8]

### Added

- Highlighting of built-in functions and constants
- Support for [semantic highlighting](https://www.jetbrains.com/help/idea/configuring-colors-and-fonts.html#semantic-highlighting)
- Settings to change the colors used by the highlighter

## [0.4.0.7]

### Added

- Support for IDEA 2023.1 EAP

### Removed

- Support for IDEA 2021.3

## [0.4.0.6]

### Added

- Support for IDEA 2022.3 EAP

### Removed

- Support for IDEA 2021.2

## [0.4.0.5]

### Fixed

- Trailing commas reported as syntax error (#46)

## [0.4.0.4]

### Added

- Support for IDEA 2022.2 EAP

### Removed

- Support for IDEA 2021.1

## [0.4.0.3]

### Added

- Support for IDEA 2022.1

## [0.4.0.2]

### Added

- Support for IDEA 2021.3

### Removed

- Support for IDEA 2020.3

## [0.4.0.1]

### Added

- Support for IDEA 2021.2

### Removed

- Support for IDEA 2020.2

## [0.4.0.0]

This release features a complete rewrite of the parser and lexer within
the plugin.

### Added

- Support for the full syntax of Nix 2.3

### Changed

- Error detection and recovery has been overhauled.
- The following words are no longer treated as keywords to make the
  implementation consistent with Nix 2.3:

  - `import`
  - `imports`
  - `require`
  - `requires`
  - `true`
  - `false`

  As a result, these words are no longer highlighted. We might bring
  back the special highlighting in a future release by using a different
  implementation for the highlighter.
- Messages for syntax errors no longer contain the *“NixTokenType.”*
  prefix for every expected token. This should make the messages much
  easier to read.

### Fixed

- Various parsing errors (including but not limited to #8 and #13)
- Incorrect reset of parser state when modifying a file

## [0.3.0.6]

### Added

- Support for IDEA 2021.1

### Removed

- Support for IDEA 2020.1

## [0.3.0.5]

### Added

- Support line comment and block comment IDEA actions

## [0.3.0.4]

### Added

- Support for IDEA 2020.3

### Removed

- Support for IDEA 2019.3

## [0.3.0.3]

### Fixed

- Change ID of plugin back from `org.nixos.idea` in version 0.3.0.0 to
  `nix-idea` from earlier versions. The different ID of version 0.3.0.0
  causes *IntelliJ* and *JetBrains Marketplace* to treat version 0.3.0.0
  as a different plugin instead of another version of the same plugin.
  **Note that if you installed version 0.3.0.0 manually from the ZIP
  file, you should uninstall it when updating to a new version.**

## [0.3.0.0]

### Changed

- Update project to build for recent IJ versions

[Unreleased]: https://github.com/NixOS/nix-idea/compare/v0.4.0.17...HEAD
[0.3.0.0]: https://github.com/NixOS/nix-idea/commits/v0.3.0.0
[0.3.0.3]: https://github.com/NixOS/nix-idea/compare/v0.3.0.0...v0.3.0.3
[0.3.0.4]: https://github.com/NixOS/nix-idea/compare/v0.3.0.3...v0.3.0.4
[0.3.0.5]: https://github.com/NixOS/nix-idea/compare/v0.3.0.4...v0.3.0.5
[0.3.0.6]: https://github.com/NixOS/nix-idea/compare/v0.3.0.5...v0.3.0.6
[0.4.0.0]: https://github.com/NixOS/nix-idea/compare/v0.3.0.6...v0.4.0.0
[0.4.0.1]: https://github.com/NixOS/nix-idea/compare/v0.4.0.0...v0.4.0.1
[0.4.0.10]: https://github.com/NixOS/nix-idea/compare/v0.4.0.9...v0.4.0.10
[0.4.0.11]: https://github.com/NixOS/nix-idea/compare/v0.4.0.10...v0.4.0.11
[0.4.0.12]: https://github.com/NixOS/nix-idea/compare/v0.4.0.11...v0.4.0.12
[0.4.0.13]: https://github.com/NixOS/nix-idea/compare/v0.4.0.12...v0.4.0.13
[0.4.0.14]: https://github.com/NixOS/nix-idea/compare/v0.4.0.13...v0.4.0.14
[0.4.0.15]: https://github.com/NixOS/nix-idea/compare/v0.4.0.14...v0.4.0.15
[0.4.0.16]: https://github.com/NixOS/nix-idea/compare/v0.4.0.15...v0.4.0.16
[0.4.0.17]: https://github.com/NixOS/nix-idea/compare/v0.4.0.16...v0.4.0.17
[0.4.0.2]: https://github.com/NixOS/nix-idea/compare/v0.4.0.1...v0.4.0.2
[0.4.0.3]: https://github.com/NixOS/nix-idea/compare/v0.4.0.2...v0.4.0.3
[0.4.0.4]: https://github.com/NixOS/nix-idea/compare/v0.4.0.3...v0.4.0.4
[0.4.0.5]: https://github.com/NixOS/nix-idea/compare/v0.4.0.4...v0.4.0.5
[0.4.0.6]: https://github.com/NixOS/nix-idea/compare/v0.4.0.5...v0.4.0.6
[0.4.0.7]: https://github.com/NixOS/nix-idea/compare/v0.4.0.6...v0.4.0.7
[0.4.0.8]: https://github.com/NixOS/nix-idea/compare/v0.4.0.7...v0.4.0.8
[0.4.0.9]: https://github.com/NixOS/nix-idea/compare/v0.4.0.8...v0.4.0.9
