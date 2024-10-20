package org.nixos.idea.lang.references

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class SymbolNavigationTest : AbstractSymbolNavigationTests() {

    @Nested
    inner class RecursiveSet {

        @TestFactory
        fun simple_assignment() = test {
            code = """
                rec {
                  <symbol><decl>subject</decl></symbol> = "...";
                  dummy = "...";
                  body = <ref>subject</ref>;
                }
                """.trimIndent()
        }

        @TestFactory
        fun child_assignment() = test {
            code = """
                rec {
                  <symbol><decl>subject</decl></symbol>.subject = "...";
                  dummy.subject = "...";
                  body = <ref>subject</ref>;
                }
                """.trimIndent()
        }

        @TestFactory
        fun multiple_assignments() = test {
            code = """
                rec { # <symbol>subject</symbol>
                  <decl>subject</decl>.child = "...";
                  dummy.subject = "...";
                  body = <ref>subject</ref>;
                  <decl>subject</decl> = "...";
                  dummy.subject = "...";
                  <decl>subject</decl>.child = "...";
                }
                """.trimIndent()
        }

        @TestFactory
        fun conflicting_declarations() = test {
            code = """
                rec { # <symbol>subject</symbol>
                  <decl>subject</decl> = "...";
                  inherit (unknown-value) <decl>subject</decl>;
                  body = <ref>subject</ref>;
                }
                """.trimIndent()
        }

        @TestFactory
        fun simple_inherit() = test {
            code = """
                rec {
                  inherit (unknown-value)
                    <symbol><decl>subject</decl></symbol>
                    dummy;
                  body = <ref>subject</ref>;
                }
                """.trimIndent()
        }

        @TestFactory
        fun recursive() = test {
            code = """
                rec { <symbol><decl>subject</decl></symbol> = <ref>subject</ref>; }
                """.trimIndent()
        }
    }

    @Nested
    inner class LetExpression {

        @TestFactory
        fun simple_assignment() = test {
            code = """
                let
                  dummy = "...";
                  <symbol><decl>subject</decl></symbol> = "...";
                in
                  <ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun child_assignment() = test {
            code = """
                let
                  dummy.subject = "...";
                  <symbol><decl>subject</decl></symbol>.subject = "...";
                in
                  <ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun multiple_assignments() = test {
            code = """
                let # <symbol>subject</symbol>
                  <decl>subject</decl>.child = "...";
                  dummy.subject = "...";
                  <decl>subject</decl> = "...";
                  dummy.subject = "...";
                  <decl>subject</decl>.child = "...";
                in
                  <ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun conflicting_declarations() = test {
            code = """
                let # <symbol>subject</symbol>
                  inherit (unknown-value) <decl>subject</decl>;
                  <decl>subject</decl> = "...";
                in
                  <ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun simple_inherit() = test {
            code = """
                let
                  inherit (unknown-value)
                    dummy
                    <symbol><decl>subject</decl></symbol>;
                in
                  <ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun recursive() = test {
            code = """
                let <symbol><decl>subject</decl></symbol> = <ref>subject</ref>;
                in <ref>subject</ref>
                """.trimIndent()
        }
    }

    @Nested
    inner class Parameter {

        @TestFactory
        fun simple_parameter() = test {
            code = """
                <symbol><decl>subject</decl></symbol>: dummy:
                <ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun formal_parameter() = test {
            code = """
                { <symbol><decl>subject</decl></symbol> ? "...", dummy }:
                <ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun conflicting_parameters() = test {
            code = """
                <symbol><decl>subject</decl></symbol> @ {
                  <decl>subject</decl> ? "...",
                  dummy,
                  <decl>subject</decl>,
                  dummy,
                  <decl>subject</decl> ? "...",
                }:
                <ref>subject</ref>
                """.trimIndent()
        }
    }

    @Nested
    inner class InheritStatement {

        @TestFactory
        fun as_reference() = test {
            code = """
                let <symbol><decl>subject</decl></symbol> = "..."; in
                { inherit dummy <ref>subject</ref>; }
                """.trimIndent()
        }

        @TestFactory
        fun recursive_shadow_trap() = test {
            // When inheriting variables from the lexical scope,
            // the inherit statement must not shadow the inherited variable.
            code = """
                let <symbol><decl>subject</decl></symbol> = "..."; in
                let inherit dummy <ref>subject</ref>; in
                rec {
                  inherit dummy <ref>subject</ref>;
                  body = <ref>subject</ref>;
                }
                """.trimIndent()
        }
    }

    @Nested
    inner class Builtins {

        @TestFactory
        @DisplayName("builtins")
        fun builtins() = test {
            code = """
                # <builtin>builtins</builtin>
                [ <ref>builtins</ref> <ref>builtins</ref>.null dummy ]
                """.trimIndent()
        }

        @TestFactory
        @DisplayName("null")
        fun null_direct() = test {
            code = """
                # <builtin>null</builtin>
                <ref>null</ref>
                """.trimIndent()
        }
    }

    @Nested
    inner class Shadowing {

        @TestFactory
        fun shadowed_by_assignment() = test {
            code = """
                let subject = "..."; in
                let inherit (unknown-value) subject; in
                let <symbol><decl>subject</decl></symbol> = "..."; in
                <ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun shadowed_by_inherit() = test {
            code = """
                let subject = "..."; in
                let inherit (unknown-value) subject; in
                let inherit (unknown-value) <symbol><decl>subject</decl></symbol>; in
                <ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun shadowed_by_recursive_set() = test {
            code = """
                let subject = "..."; in
                let inherit (unknown-value) subject; in
                rec { <symbol><decl>subject</decl></symbol> = <ref>subject</ref>; }
                """.trimIndent()
        }

        @TestFactory
        fun shadowed_builtin() = test {
            code = """
                let <symbol><decl>builtins</decl></symbol> = "..."; in
                <ref>builtins</ref>.abc
                """.trimIndent()
        }
    }

    @Nested
    inner class StringNotation {

        @TestFactory
        fun declaration() = test {
            code = """
                let <symbol><decl>"subject"</decl></symbol> = "..."; in
                <ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun with_special_character() = test {
            code = """
                let <symbol><decl>"my very special variable ⇐"</decl></symbol> = "..."; in
                { inherit <ref>"my very special variable ⇐"</ref>; }
                """.trimIndent()
        }

        @TestFactory
        // TODO: Maybe use some custom index in NixUsageSearcher?
        @Disabled("NixUsageSearcher uses text search, so it only finds usages where the text matches the symbol")
        fun with_escape_sequence() = test {
            // symbol name: $$$
            code = """
                let <symbol><decl>"${"$\\$$"}"</decl></symbol> = "..."; in
                { inherit <ref>"${"$\\$$"}"</ref>; }
                """.trimIndent()
        }

        @TestFactory
        // TODO: Maybe use some custom index in NixUsageSearcher?
        @Disabled("NixUsageSearcher uses text search, so it only finds usages where the text matches the symbol")
        fun with_escape_sequence_non_normalized() = test {
            // symbol name: $$$
            code = """
                let <symbol><decl>"${"$\\$$"}"</decl></symbol> = "..."; in
                { inherit <ref>"${"$$\\$"}"</ref>; }
                """.trimIndent()
        }
    }
}
