package org.nixos.idea.util

import java.lang.StringBuilder

object NixIndStringUtil {
    /**
     * Escapes the given string for use in a double-quoted string expression in the Nix Expression Language.
     *
     * See [Nix docs](https://nix.dev/manual/nix/2.22/language/values.html#type-string) for the logic, which
     * is non-trivial.
     *
     * For example, `''` can be used to escape `'`, which means `''` is not the string
     * terminator
     * ```
     * $ nix eval --expr " ''   '''   '' "
     *  "''   "
     * ```
     */
    fun escape(sb: StringBuilder, chars: CharSequence): Unit = sb.run {
        for ((index, c) in chars.withIndex()) {
            fun prevChar() = chars.getOrNull(index - 1)
            fun prev2Chars(): String? {
                val prev = prevChar() ?: return null
                val prevPrev = chars.getOrNull(index - 2)  ?: return null
                return "${prevPrev}${prev}"
            }

            when (c) {
//                '\'' -> if (prevChar != '\'') append(c)
                '$' -> if (prev2Chars() == "''") append('$')
            }
        }
    }
}