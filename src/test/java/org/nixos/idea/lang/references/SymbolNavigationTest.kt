package org.nixos.idea.lang.references

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestFactory

class SymbolNavigationTest : AbstractSymbolNavigationTests() {

    @TestFactory
    fun simple_assignment() = test {
        code = """
                let <symbol><decl>some-variable</decl></symbol> = "..."; in
                <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun child_assignment() = test {
        code = """
                let <symbol><decl>some-variable</decl></symbol>.abc = "..."; in
                <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun simple_inherit() = test {
        code = """
                let inherit (unknown-value) <symbol><decl>some-variable</decl></symbol>; in
                <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun recursive_inherit() = test {
        code = """
                let <symbol><decl>some-variable</decl></symbol> = "..."; in
                let inherit <ref>some-variable</ref>; in
                <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun from_inherit() = test {
        code = """
                let <symbol><decl>some-variable</decl></symbol> = "..."; in
                { inherit <ref>some-variable</ref>; }
                """.trimIndent()
    }

    @TestFactory
    fun simple_parameter() = test {
        code = """
                <symbol><decl>some-variable</decl></symbol>:
                <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun formal_parameter() = test {
        code = """
                { <symbol><decl>some-variable</decl></symbol> ? "..." }:
                <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun multiple_declarations() = test {
        code = """
                let
                  # <symbol>some-variable</symbol>
                  <decl>some-variable</decl>.aaa = "...";
                  <decl>some-variable</decl>.bbb = "...";
                  <decl>some-variable</decl>.x.y = "...";
                in
                  <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun conflicting_declarations() = test {
        code = """
                let
                  inherit (unknown-value) <symbol><decl>some-variable</decl></symbol>;
                  <decl>some-variable</decl> = "...";
                in
                  <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun shadowed_by_assignment() = test {
        code = """
                let some-variable = "..."; in
                let inherit (unknown-value) some-variable; in
                let <symbol><decl>some-variable</decl></symbol> = "..."; in
                <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun shadowed_by_inherit() = test {
        code = """
                let some-variable = "..."; in
                let inherit (unknown-value) some-variable; in
                let inherit (unknown-value) <symbol><decl>some-variable</decl></symbol>; in
                <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun string_notation_declaration() = test {
        code = """
                let <symbol><decl>"some-variable"</decl></symbol> = "..."; in
                <ref>some-variable</ref>
                """.trimIndent()
    }

    @TestFactory
    fun string_notation_with_special_character() = test {
        code = """
                let <symbol><decl>"my very special variable ⇐"</decl></symbol> = "..."; in
                { inherit <ref>"my very special variable ⇐"</ref>; }
                """.trimIndent()
    }

    @TestFactory
    // TODO: Maybe use some custom index in NixUsageSearcher?
    @Disabled("NixUsageSearcher uses text search, so it only finds usages where the text matches the symbol")
    fun string_notation_with_escape_sequences() = test {
        // symbol name: $$$
        code = """
                let <symbol><decl>"${"$\\$$"}"</decl></symbol> = "..."; in
                { inherit <ref>"${"$\\$$"}"</ref>; }
                """.trimIndent()
    }

    @TestFactory
    // TODO: Maybe use some custom index in NixUsageSearcher?
    @Disabled("NixUsageSearcher uses text search, so it only finds usages where the text matches the symbol")
    fun string_notation_with_escape_sequences_non_normalized() = test {
        // symbol name: $$$
        code = """
                let <symbol><decl>"${"$\\$$"}"</decl></symbol> = "..."; in
                { inherit <ref>"${"$$\\$"}"</ref>; }
                """.trimIndent()
    }

    @TestFactory
    fun builtin() = test {
        code = """
                # <builtin>builtins</builtin>
                [ <ref>builtins</ref> <ref>builtins</ref>.abc ]
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
