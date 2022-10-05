# Changelog

## [Unreleased]
### Added
- Support for IDEA 2022.3 EAP

### Changed

### Deprecated

### Removed
- Support for IDEA 2021.2

### Fixed

### Security

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
