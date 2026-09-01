// port-lint: source matcher/lib.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.chars.AsciiChar
import io.github.kotlinmania.nucleo.chars.normalize

/**
 * A matcher engine that can execute (fuzzy) matches.
 */
public class Matcher(
    public var config: Config = Config.DEFAULT,
) {
    internal val slab: MatrixSlab = MatrixSlab.new()

    /**
     * Creates a copy of this matcher.
     */
    public fun clone(): Matcher = Matcher(config.clone())

    /**
     * Formats the matcher into a debug string.
     */
    public fun fmt(): String = "Matcher { config: $config }"

    override fun toString(): String = fmt()

    public companion object {
        /**
         * Creates a new matcher instance with the given configuration.
         */
        public fun new(config: Config = Config.DEFAULT): Matcher = Matcher(config)

        /**
         * Creates a default matcher instance.
         */
        public fun default(): Matcher = Matcher(Config.DEFAULT)
    }

    /**
     * Find the fuzzy match with the highest score in the haystack.
     */
    public fun fuzzyMatch(haystack: Utf32Str, needle: Utf32Str): Int? = fuzzyMatcherImpl(haystack, needle, null)

    /**
     * Find the fuzzy match with the highest score in the haystack and compute its indices.
     */
    public fun fuzzyIndices(haystack: Utf32Str, needle: Utf32Str, indices: MutableList<Int>): Int? = fuzzyMatcherImpl(haystack, needle, indices)

    private fun fuzzyMatcherImpl(
        haystack: Utf32Str,
        needle: Utf32Str,
        indices: MutableList<Int>?,
    ): Int? {
        if (needle.length > haystack.length) return null
        if (needle.isEmpty()) return 0
        if (needle.length == haystack.length) {
            return exactMatchImpl(haystack, needle, 0, haystack.length, indices)
        }

        return when {
            haystack is Utf32Str.Ascii && needle is Utf32Str.Ascii -> {
                val haystackBytes = haystack.toByteArray()
                val needleBytes = needle.toByteArray()
                if (needle.length == 1) {
                    return substringMatch1Ascii(config, haystackBytes, needleBytes[0], indices)
                }
                val pre = prefilterAscii(config, haystackBytes, needleBytes, onlyGreedy = false) ?: return null
                if (needle.length == pre.end - pre.start) {
                    return calculateScoreAscii(config, haystackBytes, needleBytes, pre.start, pre.greedyEnd, indices)
                }
                fuzzyMatchOptimalAscii(config, haystackBytes, needleBytes, pre.start, pre.greedyEnd, pre.end, indices)
            }
            haystack is Utf32Str.Ascii && needle is Utf32Str.Unicode -> {
                null
            }
            haystack is Utf32Str.Unicode && needle is Utf32Str.Ascii -> {
                val haystackChars = haystack.toCharArray()
                if (needle.length == 1) {
                    val pre = prefilterNonAscii(config, haystackChars, needle, onlyGreedy = true) ?: return null
                    return substringMatch1NonAscii(config, haystackChars, needle.get(0), pre.start, indices)
                }
                val pre = prefilterNonAscii(config, haystackChars, needle, onlyGreedy = false) ?: return null
                if (needle.length == pre.end - pre.start) {
                    return exactMatchImpl(haystack, needle, pre.start, pre.end, indices)
                }
                val needleChars = CharArray(needle.length) { needle.get(it) }
                fuzzyMatchOptimalUnicode(config, haystackChars, needleChars, pre.start, pre.start + 1, pre.end, indices)
            }
            haystack is Utf32Str.Unicode && needle is Utf32Str.Unicode -> {
                val haystackChars = haystack.toCharArray()
                if (needle.length == 1) {
                    val pre = prefilterNonAscii(config, haystackChars, needle, onlyGreedy = true) ?: return null
                    return substringMatch1NonAscii(config, haystackChars, needle.get(0), pre.start, indices)
                }
                val pre = prefilterNonAscii(config, haystackChars, needle, onlyGreedy = false) ?: return null
                if (needle.length == pre.end - pre.start) {
                    return exactMatchImpl(haystack, needle, pre.start, pre.end, indices)
                }
                val needleChars = CharArray(needle.length) { needle.get(it) }
                fuzzyMatchOptimalUnicode(config, haystackChars, needleChars, pre.start, pre.start + 1, pre.end, indices)
            }
            else -> null
        }
    }

    /**
     * Greedily find a fuzzy match in the haystack.
     */
    public fun fuzzyMatchGreedy(haystack: Utf32Str, needle: Utf32Str): Int? = fuzzyMatchGreedyImpl(haystack, needle, null)

    /**
     * Greedily find a fuzzy match in the haystack and compute its indices.
     */
    public fun fuzzyIndicesGreedy(haystack: Utf32Str, needle: Utf32Str, indices: MutableList<Int>): Int? = fuzzyMatchGreedyImpl(haystack, needle, indices)

    private fun fuzzyMatchGreedyImpl(
        haystack: Utf32Str,
        needle: Utf32Str,
        indices: MutableList<Int>?,
    ): Int? {
        if (needle.length > haystack.length) return null
        if (needle.isEmpty()) return 0
        if (needle.length == haystack.length) {
            return exactMatchImpl(haystack, needle, 0, haystack.length, indices)
        }

        return when {
            haystack is Utf32Str.Ascii && needle is Utf32Str.Ascii -> {
                val haystackBytes = haystack.toByteArray()
                val needleBytes = needle.toByteArray()
                val pre = prefilterAscii(config, haystackBytes, needleBytes, onlyGreedy = true) ?: return null
                if (needle.length == pre.greedyEnd - pre.start) {
                    return calculateScoreAscii(config, haystackBytes, needleBytes, pre.start, pre.greedyEnd, indices)
                }
                fuzzyMatchGreedyAscii(config, haystackBytes, needleBytes, pre.start, pre.greedyEnd, indices)
            }
            haystack is Utf32Str.Ascii && needle is Utf32Str.Unicode -> {
                null
            }
            haystack is Utf32Str.Unicode && needle is Utf32Str.Ascii -> {
                val haystackChars = haystack.toCharArray()
                val pre = prefilterNonAscii(config, haystackChars, needle, onlyGreedy = true) ?: return null
                val needleChars = CharArray(needle.length) { needle.get(it) }
                fuzzyMatchGreedyUnicode(config, haystackChars, needleChars, pre.start, pre.start + 1, indices)
            }
            haystack is Utf32Str.Unicode && needle is Utf32Str.Unicode -> {
                val haystackChars = haystack.toCharArray()
                val pre = prefilterNonAscii(config, haystackChars, needle, onlyGreedy = true) ?: return null
                val needleChars = CharArray(needle.length) { needle.get(it) }
                fuzzyMatchGreedyUnicode(config, haystackChars, needleChars, pre.start, pre.start + 1, indices)
            }
            else -> null
        }
    }

    /**
     * Finds the substring match with the highest score in the haystack.
     */
    public fun substringMatch(haystack: Utf32Str, needle: Utf32Str): Int? = substringMatchImpl(haystack, needle, null)

    /**
     * Finds the substring match with the highest score in the haystack and compute its indices.
     */
    public fun substringIndices(haystack: Utf32Str, needle: Utf32Str, indices: MutableList<Int>): Int? = substringMatchImpl(haystack, needle, indices)

    private fun substringMatchImpl(
        haystack: Utf32Str,
        needle: Utf32Str,
        indices: MutableList<Int>?,
    ): Int? {
        if (needle.length > haystack.length) return null
        if (needle.isEmpty()) return 0
        if (needle.length == haystack.length) {
            return exactMatchImpl(haystack, needle, 0, haystack.length, indices)
        }

        return when {
            haystack is Utf32Str.Ascii && needle is Utf32Str.Ascii -> {
                val haystackBytes = haystack.toByteArray()
                val needleBytes = needle.toByteArray()
                if (needle.length == 1) {
                    return substringMatch1Ascii(config, haystackBytes, needleBytes[0], indices)
                }
                substringMatchAscii(config, haystackBytes, needleBytes, indices)
            }
            haystack is Utf32Str.Ascii && needle is Utf32Str.Unicode -> {
                null
            }
            haystack is Utf32Str.Unicode && needle is Utf32Str.Ascii -> {
                val haystackChars = haystack.toCharArray()
                if (needle.length == 1) {
                    val pre = prefilterNonAscii(config, haystackChars, needle, onlyGreedy = true) ?: return null
                    return substringMatch1NonAscii(config, haystackChars, needle.get(0), pre.start, indices)
                }
                val pre = prefilterNonAscii(config, haystackChars, needle, onlyGreedy = false) ?: return null
                substringMatchNonAscii(config, haystackChars, needle, pre.start, indices)
            }
            haystack is Utf32Str.Unicode && needle is Utf32Str.Unicode -> {
                val haystackChars = haystack.toCharArray()
                if (needle.length == 1) {
                    val pre = prefilterNonAscii(config, haystackChars, needle, onlyGreedy = true) ?: return null
                    return substringMatch1NonAscii(config, haystackChars, needle.get(0), pre.start, indices)
                }
                val pre = prefilterNonAscii(config, haystackChars, needle, onlyGreedy = false) ?: return null
                substringMatchNonAscii(config, haystackChars, needle, pre.start, indices)
            }
            else -> null
        }
    }

    /**
     * Checks whether needle and haystack match exactly.
     */
    public fun exactMatch(haystack: Utf32Str, needle: Utf32Str): Int? {
        if (needle.isEmpty()) return 0
        val leadingSpace = if (!needle.first().isWhitespace()) haystack.leadingWhiteSpace() else 0
        val trailingSpace = if (!needle.last().isWhitespace()) haystack.trailingWhiteSpace() else 0
        if (trailingSpace == haystack.length) return null
        return exactMatchImpl(haystack, needle, leadingSpace, haystack.length - trailingSpace, null)
    }

    /**
     * Checks whether needle and haystack match exactly and computes the match indices.
     */
    public fun exactIndices(haystack: Utf32Str, needle: Utf32Str, indices: MutableList<Int>): Int? {
        if (needle.isEmpty()) return 0
        val leadingSpace = if (!needle.first().isWhitespace()) haystack.leadingWhiteSpace() else 0
        val trailingSpace = if (!needle.last().isWhitespace()) haystack.trailingWhiteSpace() else 0
        if (trailingSpace == haystack.length) return null
        return exactMatchImpl(haystack, needle, leadingSpace, haystack.length - trailingSpace, indices)
    }

    /**
     * Checks whether needle is a prefix of the haystack.
     */
    public fun prefixMatch(haystack: Utf32Str, needle: Utf32Str): Int? {
        if (needle.isEmpty()) return 0
        val leadingSpace = if (!needle.first().isWhitespace()) haystack.leadingWhiteSpace() else 0
        if (haystack.length - leadingSpace < needle.length) return null
        return exactMatchImpl(haystack, needle, leadingSpace, needle.length + leadingSpace, null)
    }

    /**
     * Checks whether needle is a prefix of the haystack and computes match indices.
     */
    public fun prefixIndices(haystack: Utf32Str, needle: Utf32Str, indices: MutableList<Int>): Int? {
        if (needle.isEmpty()) return 0
        val leadingSpace = if (!needle.first().isWhitespace()) haystack.leadingWhiteSpace() else 0
        if (haystack.length - leadingSpace < needle.length) return null
        return exactMatchImpl(haystack, needle, leadingSpace, needle.length + leadingSpace, indices)
    }

    /**
     * Checks whether needle is a postfix of the haystack.
     */
    public fun postfixMatch(haystack: Utf32Str, needle: Utf32Str): Int? {
        if (needle.isEmpty()) return 0
        val trailingSpaces = if (!needle.last().isWhitespace()) haystack.trailingWhiteSpace() else 0
        if (haystack.length - trailingSpaces < needle.length) return null
        return exactMatchImpl(haystack, needle, haystack.length - needle.length - trailingSpaces, haystack.length - trailingSpaces, null)
    }

    /**
     * Checks whether needle is a postfix of the haystack and computes match indices.
     */
    public fun postfixIndices(haystack: Utf32Str, needle: Utf32Str, indices: MutableList<Int>): Int? {
        if (needle.isEmpty()) return 0
        val trailingSpaces = if (!needle.last().isWhitespace()) haystack.trailingWhiteSpace() else 0
        if (haystack.length - trailingSpaces < needle.length) return null
        return exactMatchImpl(haystack, needle, haystack.length - needle.length - trailingSpaces, haystack.length - trailingSpaces, indices)
    }

    private fun exactMatchImpl(
        haystack: Utf32Str,
        needle: Utf32Str,
        start: Int,
        end: Int,
        indices: MutableList<Int>?,
    ): Int? {
        if (needle.length != end - start) return null

        return when {
            haystack is Utf32Str.Ascii && needle is Utf32Str.Ascii -> {
                val hBytes = haystack.toByteArray()
                val nBytes = needle.toByteArray()
                val matched =
                    if (config.ignoreCase) {
                        var m = true
                        for (i in 0 until needle.length) {
                            if (AsciiChar(hBytes[start + i]).normalize(config) != AsciiChar(nBytes[i]).normalize(config)) {
                                m = false
                                break
                            }
                        }
                        m
                    } else {
                        var m = true
                        for (i in 0 until needle.length) {
                            if (hBytes[start + i] != nBytes[i]) {
                                m = false
                                break
                            }
                        }
                        m
                    }
                if (!matched) return null
                calculateScoreAscii(config, hBytes, nBytes, start, end, indices)
            }
            haystack is Utf32Str.Ascii && needle is Utf32Str.Unicode -> {
                null
            }
            haystack is Utf32Str.Unicode && needle is Utf32Str.Ascii -> {
                val hChars = haystack.toCharArray()
                val nChars = CharArray(needle.length) { needle.get(it) }
                var matched = true
                for (i in 0 until needle.length) {
                    if (normalize(hChars[start + i], config) != normalize(nChars[i], config)) {
                        matched = false
                        break
                    }
                }
                if (!matched) return null
                calculateScoreUnicode(config, hChars, nChars, start, end, indices)
            }
            haystack is Utf32Str.Unicode && needle is Utf32Str.Unicode -> {
                val hChars = haystack.toCharArray()
                val nChars = needle.toCharArray()
                var matched = true
                for (i in 0 until needle.length) {
                    if (normalize(hChars[start + i], config) != normalize(nChars[i], config)) {
                        matched = false
                        break
                    }
                }
                if (!matched) return null
                calculateScoreUnicode(config, hChars, nChars, start, end, indices)
            }
            else -> null
        }
    }
}
