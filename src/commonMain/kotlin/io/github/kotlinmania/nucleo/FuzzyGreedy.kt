// port-lint: source nucleo/matcher/src/fuzzy_greedy.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.chars.AsciiChar
import io.github.kotlinmania.nucleo.chars.normalize

internal fun fuzzyMatchGreedyAscii(
    config: Config,
    haystack: ByteArray,
    needle: ByteArray,
    start: Int,
    end: Int,
    indices: MutableList<Int>?,
): Int? {
    var actualStart = start
    val actualEnd = end

    var needleIdx = needle.size - 1
    var needleChar = AsciiChar(needle[needleIdx]).normalize(config)

    for (i in (actualEnd - 1) downTo actualStart) {
        val c = AsciiChar(haystack[i]).normalize(config)
        if (c == needleChar) {
            needleIdx--
            if (needleIdx < 0) {
                actualStart = i
                break
            }
            needleChar = AsciiChar(needle[needleIdx]).normalize(config)
        }
    }
    return calculateScoreAscii(config, haystack, needle, actualStart, actualEnd, indices)
}

internal fun fuzzyMatchGreedyUnicode(
    config: Config,
    haystack: CharArray,
    needle: CharArray,
    start: Int,
    end: Int,
    indices: MutableList<Int>?,
): Int? {
    var actualStart = start
    var actualEnd = end

    if (needle.size > 1) {
        var needleIdx = 1
        var needleChar = needle[needleIdx]
        var matchedAll = false
        val firstCharEnd = start + 1
        for (i in firstCharEnd until haystack.size) {
            if (normalize(haystack[i], config) == needleChar) {
                needleIdx++
                if (needleIdx >= needle.size) {
                    actualEnd = i + 1
                    matchedAll = true
                    break
                }
                needleChar = needle[needleIdx]
            }
        }
        if (!matchedAll) return null
    }

    var needleIdx = needle.size - 1
    var needleChar = needle[needleIdx]
    for (i in (actualEnd - 1) downTo actualStart) {
        val c = normalize(haystack[i], config)
        if (c == needleChar) {
            needleIdx--
            if (needleIdx < 0) {
                actualStart = i
                break
            }
            needleChar = needle[needleIdx]
        }
    }
    return calculateScoreUnicode(config, haystack, needle, actualStart, actualEnd, indices)
}

internal fun Matcher.fuzzyMatchGreedy_(
    haystack: ByteArray,
    needle: ByteArray,
    start: Int,
    end: Int,
    indices: MutableList<Int>? = null,
): Int? = fuzzyMatchGreedyAscii(config, haystack, needle, start, end, indices)

internal fun Matcher.fuzzyMatchGreedy_(
    haystack: CharArray,
    needle: CharArray,
    start: Int,
    end: Int,
    indices: MutableList<Int>? = null,
): Int? = fuzzyMatchGreedyUnicode(config, haystack, needle, start, end, indices)
