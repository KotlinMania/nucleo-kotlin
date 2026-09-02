// port-lint: source matcher/src/chars.rs
package io.github.kotlinmania.nucleo.chars

import io.github.kotlinmania.nucleo.Config

/**
 * Character classification for boundary and scoring heuristics.
 */
public enum class CharClass {
    Whitespace,
    NonWord,
    Delimiter,
    Lower,
    Upper,
    Letter,
    Number,
}

/**
 * Trait for character classification and normalization operations matching upstream `Char` trait.
 */
public interface CharTrait<Self> {
    public val isAscii: Boolean
    public fun charClass(config: Config): CharClass
    public fun charClassAndNormalize(config: Config): Pair<Self, CharClass>
    public fun normalize(config: Config): Self
}

/**
 * Wrapper around an ASCII byte value with character classification and normalization methods.
 */
@kotlin.jvm.JvmInline
public value class AsciiChar(
    public val byte: Byte,
) : Comparable<AsciiChar>, CharTrait<AsciiChar> {
    override val isAscii: Boolean get() = true
    public constructor(char: Char) : this(char.code.toByte())
    public constructor(code: Int) : this(code.toByte())

    public val code: Int get() = byte.toInt() and 0xFF
    public val toChar: Char get() = (byte.toInt() and 0xFF).toChar()

    override fun compareTo(other: AsciiChar): Int = (byte.toInt() and 0xFF).compareTo(other.byte.toInt() and 0xFF)

    public fun fmt(): String = toChar.toString()

    public fun eq(other: Char): Boolean = toChar == other

    public fun eq(other: AsciiChar): Boolean = byte == other.byte

    override fun toString(): String = fmt()

    override fun charClass(config: Config): CharClass {
        val c = byte.toInt() and 0xFF
        return if (c in 0x61..0x7A) {
            CharClass.Lower
        } else if (c in 0x41..0x5A) {
            CharClass.Upper
        } else if (c in 0x30..0x39) {
            CharClass.Number
        } else if (c == 0x20 || c == 0x09 || c == 0x0A || c == 0x0C || c == 0x0D) {
            CharClass.Whitespace
        } else if (config.delimiterChars.contains(byte)) {
            CharClass.Delimiter
        } else {
            CharClass.NonWord
        }
    }

    override fun charClassAndNormalize(config: Config): Pair<AsciiChar, CharClass> {
        val clazz = charClass(config)
        val normalized =
            if (config.ignoreCase && clazz == CharClass.Upper) {
                AsciiChar((byte + 32).toByte())
            } else {
                this
            }
        return Pair(normalized, clazz)
    }

    override fun normalize(config: Config): AsciiChar {
        val c = byte.toInt() and 0xFF
        return if (config.ignoreCase && c in 0x41..0x5A) {
            AsciiChar((byte + 32).toByte())
        } else {
            this
        }
    }

    public companion object {
        public const val ASCII: Boolean = true

        public fun cast(bytes: ByteArray): List<AsciiChar> = bytes.map { AsciiChar(it) }
    }
}

/**
 * Converts a character to lowercase using simple Unicode case folding.
 */
public fun toLowerCase(c: Char): Char {
    val code = c.code
    val idx = binarySearchCaseFold(code)
    return if (idx >= 0) CASE_FOLDING_SIMPLE[idx].second.toChar() else c.lowercaseChar()
}

/**
 * Checks if a character is uppercase according to simple Unicode case folding.
 */
public fun isUpperCase(c: Char): Boolean {
    val code = c.code
    if (binarySearchCaseFold(code) >= 0) return true
    return c.isUpperCase()
}

private fun binarySearchCaseFold(code: Int): Int {
    var low = 0
    var high = CASE_FOLDING_SIMPLE.size - 1
    while (low <= high) {
        val mid = (low + high) ushr 1
        val midVal = CASE_FOLDING_SIMPLE[mid].first
        if (midVal < code) {
            low = mid + 1
        } else if (midVal > code) {
            high = mid - 1
        } else {
            return mid
        }
    }
    return -(low + 1)
}

/**
 * Classifies a non-ASCII character into [CharClass].
 */
public fun charClassNonAscii(c: Char): CharClass =
    if (c.isLowerCase()) {
        CharClass.Lower
    } else if (isUpperCase(c)) {
        CharClass.Upper
    } else if (c.isDigit()) {
        CharClass.Number
    } else if (c.isLetter()) {
        CharClass.Letter
    } else if (c.isWhitespace()) {
        CharClass.Whitespace
    } else {
        CharClass.NonWord
    }

/**
 * Classifies a character into [CharClass] according to the matcher config.
 */
public fun charClass(c: Char, config: Config): CharClass {
    if (c.code <= 127) {
        return AsciiChar(c).charClass(config)
    }
    return charClassNonAscii(c)
}

/**
 * Classifies and normalizes a character according to the matcher config.
 */
public fun charClassAndNormalize(c: Char, config: Config): Pair<Char, CharClass> {
    if (c.code <= 127) {
        val (normalized, clazz) = AsciiChar(c).charClassAndNormalize(config)
        return Pair(normalized.toChar, clazz)
    }
    val clazz = charClassNonAscii(c)
    var caseFold = clazz == CharClass.Upper
    var current = c
    if (config.normalize) {
        current = normalize(current)
        caseFold = true
    }
    if (caseFold && config.ignoreCase) {
        current = toLowerCase(current)
    }
    return Pair(current, clazz)
}

/**
 * Normalizes a character according to the matcher config.
 */
public fun normalize(c: Char, config: Config): Char {
    var current = c
    if (config.normalize) {
        current = normalize(current)
    }
    if (config.ignoreCase) {
        current = toLowerCase(current)
    }
    return current
}

/**
 * Returns the first character of each Unicode grapheme in a string.
 */
public fun graphemes(text: String): Sequence<Char> =
    sequence {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') {
                yield('\n')
                i += 2
            } else {
                yield(c)
                i++
                while (i < text.length) {
                    val next = text[i]
                    if (next == '\r' || next == '\n') break
                    val cat = next.category
                    if (cat == CharCategory.NON_SPACING_MARK ||
                        cat == CharCategory.COMBINING_SPACING_MARK ||
                        cat == CharCategory.ENCLOSING_MARK ||
                        cat == CharCategory.FORMAT ||
                        next == '\u200D' ||
                        (next.code in 0xFE00..0xFE0F)
                    ) {
                        i++
                    } else {
                        break
                    }
                }
            }
        }
    }
