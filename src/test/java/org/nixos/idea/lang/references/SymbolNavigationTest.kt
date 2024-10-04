package org.nixos.idea.lang.references

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class SymbolNavigationTest : AbstractSymbolNavigationTests() {

    @Nested
    inner class AttributeSet {

        @TestFactory
        fun simple_assignment() = test {
            code = """
                {
                  dummy = "...";
                  <symbol><decl>subject</decl></symbol> = "...";
                }
                .<ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun child_assignment() = test {
            code = """
                {
                  dummy.subject = "...";
                  <symbol><decl>subject</decl></symbol>.subject = "...";
                }
                .<ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun multiple_assignments() = test {
            code = """
                { # <symbol>subject</symbol>
                  <decl>subject</decl>.child = "...";
                  dummy.subject = "...";
                  <decl>subject</decl> = "...";
                  dummy.subject = "...";
                  <decl>subject</decl>.child = "...";
                }
                .<ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun conflicting_declarations() = test {
            code = """
                { # <symbol>subject</symbol>
                  inherit (unknown-value) <decl>subject</decl>;
                  <decl>subject</decl> = "...";
                }
                .<ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun simple_inherit() = test {
            code = """
                {
                  inherit (unknown-value)
                    dummy
                    <symbol><decl>subject</decl></symbol>;
                }
                .<ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun not_recursive() = test {
            code = """
                let <symbol><decl>subject</decl></symbol> = "..."; in
                {
                  subject = "..."; # Does not shadow subject
                  body = <ref>subject</ref>;
                }
                """.trimIndent()
        }
    }

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
                .<ref>subject</ref>
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
                .<ref>subject</ref>
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
                .<ref>subject</ref>
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
                .<ref>subject</ref>
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
                .<ref>subject</ref>
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
    inner class LegacyLetExpression {

        @TestFactory
        fun body() = test {
            code = """
                let { <symbol>body.<decl>subject</decl></symbol> = "..."; dummy = "..."; }
                .<ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun recursive() = test {
            code = """
                let { <symbol><decl>subject</decl></symbol> = <ref>subject</ref>; }
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
        fun as_declaration() = test {
            // TODO: Do we also want to directly resolve the original symbol?
            code = """
                let subject = "..."; in
                { inherit dummy <symbol><decl>subject</decl></symbol>; }
                .<ref>subject</ref>
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
                .<ref>subject</ref>
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
        @DisplayName("builtins.null")
        fun builtins_null() = test {
            code = """
                # <builtin>null</builtin>
                builtins.<ref>null</ref>
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
        fun reference() = test {
            code = """
                { <symbol><decl>subject</decl></symbol> = "..."; }
                .<ref>"subject"</ref>
                """.trimIndent()
        }

        @TestFactory
        fun variable_with_special_character() = test {
            code = """
                let <symbol><decl>"my very special variable ⇐"</decl></symbol> = "..."; in
                { inherit <ref>"my very special variable ⇐"</ref>; }
                """.trimIndent()
        }

        @TestFactory
        fun attribute_with_special_character() = test {
            code = """
                { <symbol><decl>"my very special variable ⇐"</decl></symbol> = "..."; }
                .<ref>"my very special variable ⇐"</ref>
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

    @Nested
    inner class Complex {

        @TestFactory
        fun merged_attribute_set() = test {
            code = """
                let
                  merged = { <symbol><decl>subject</decl></symbol> = "a"; };
                  <symbol>merged.<decl>subject</decl></symbol> = "b";
                  merged = { inherit (unknown-value) <symbol><decl>subject</decl></symbol>; };
                in
                  merged.<ref>subject</ref>
                """.trimIndent()
        }

        @TestFactory
        fun endless_recursion_1() = test {
            // Verifies that we don't run into an endless loop or stack overflow.
            // The test tries to resolve the reference, but there is no matching symbol.
            code = """
                rec {
                  recursive = recursive.<ref>unreachable</ref>;
                }
                """.trimIndent()
        }

        @TestFactory
        fun endless_recursion_2() = test {
            // Verifies that we don't run into an endless loop or stack overflow.
            // The test tries to resolve the reference, but there is no matching symbol.
            code = """
                let
                  a = b;
                  b = a;
                in
                  a.<ref>unreachable</ref>
                """.trimIndent()
        }

        @TestFactory
        fun endless_recursion_3() = test {
            // Verifies that we don't run into an endless loop or stack overflow.
            // The test tries to resolve the references, but there is no matching symbol.
            code = """
                rec {
                  inherit (b) <ref>a</ref>;
                  inherit (a) <ref>b</ref>;
                }
                """.trimIndent()
        }

        @TestFactory
        fun finite_recursion() = test {
            // Verifies that the detection of endless recursion doesn't block simple finite recursions.
            code = """
                let
                  <symbol>a.<decl>a</decl></symbol> = b;
                  b = a;
                in
                  b.<ref>a</ref>.<ref>a</ref>.<ref>a</ref>.<ref>a</ref>.<ref>a</ref>.<ref>a</ref>.<ref>a</ref>
                """.trimIndent()
        }
    }
}
