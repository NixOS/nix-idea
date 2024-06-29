package org.nixos.idea.util

object NixIndStringUtil {
    /**
     * Escapes the given string for use in a double-quoted string expression in the Nix Expression Language.
     *
     * See [Nix docs](https://nix.dev/manual/nix/2.22/language/values.html#type-string) for the logic, which
     * is non-trivial.
     *
     * For example, `'` can be used to escape `''`, which means `'''` does not contain
     * a string terminator
     * ```
     * $ nix eval --expr " ''   '''   '' "
     *  "''   "
     * ```
     *
     * This function does not erase string interpolations, because
     * they are hard to parse in a loop without a proper grammar. For example:
     * ```nix
     * '' ${someNixFunc "${foo "}}" }" } ''
     * ```
     */
    @JvmStatic
    fun escape(chars: CharSequence): String = buildString {
        for ((index, c) in chars.withIndex()) {
            fun prevChar() = chars.getOrNull(index - 1)
            fun prev2Chars(): String? {
                val prev = prevChar() ?: return null
                val prevPrev = chars.getOrNull(index - 2) ?: return null
                return "${prevPrev}${prev}"
            }

            fun prev3Chars(): String? {
                val prev2 = prev2Chars() ?: return null
                val prevPrev2 = chars.getOrNull(index - 3) ?: return null
                return "${prevPrev2}${prev2}"
            }

            when (c) {
                // ''\ escapes any character, but we can only cover known ones in advance:
                '\'' -> when {
                    // ''' is escaped to ''
                    prev2Chars() == "''" -> append("''")
                    // ''  is the string delimiter
                    else -> continue
                }

                '\\' -> when {
                    prev2Chars() == "''" -> continue
                    prevChar() == '\'' -> continue
                    else -> append(c)
                }

                '$' -> if (prevChar() == '$') append(c) else continue
                '{' -> if (prevChar() == '$') append("\${") else append(c)

                else -> if (prev3Chars() == "''\\") when (c) {
                    'r' -> if (prev3Chars() == "''\\") append('\r') else append(c)
                    'n' -> if (prev3Chars() == "''\\") append('\n') else append(c)
                    't' -> if (prev3Chars() == "''\\") append('\t') else append(c)
                    else -> append("''\\").append(c)
                } else {
                    append(c)
                }
            }
        }
    }
}