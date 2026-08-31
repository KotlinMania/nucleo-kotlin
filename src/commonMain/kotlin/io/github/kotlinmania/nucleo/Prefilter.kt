// port-lint: source matcher/src/prefilter.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.chars.normalize

internal fun findAscii(c: Byte, haystack: ByteArray, startIndex: Int = 0, endIndex: Int = haystack.size): Int {
    for (i in startIndex until endIndex) {
        if (haystack[i] == c) return i
    }
    return -1
}

internal fun findAsciiRev(c: Byte, haystack: ByteArray, startIndex: Int = 0, endIndex: Int = haystack.size): Int {
    for (i in (endIndex - 1) downTo startIndex) {
        if (haystack[i] == c) return i
    }
    return -1
}

internal fun findAsciiIgnoreCase(c: Byte, haystack: ByteArray, startIndex: Int = 0, endIndex: Int = haystack.size): Int {
    val b = c.toInt() and 0xFF
    if (b in 0x61..0x7A) {
        val upper = (b - 32).toByte()
        val lower = c
        for (i in startIndex until endIndex) {
            val h = haystack[i]
            if (h == lower || h == upper) return i
        }
        return -1
    } else {
        return findAscii(c, haystack, startIndex, endIndex)
    }
}

internal fun findAsciiIgnoreCaseRev(c: Byte, haystack: ByteArray, startIndex: Int = 0, endIndex: Int = haystack.size): Int {
    val b = c.toInt() and 0xFF
    if (b in 0x61..0x7A) {
        val upper = (b - 32).toByte()
        val lower = c
        for (i in (endIndex - 1) downTo startIndex) {
            val h = haystack[i]
            if (h == lower || h == upper) return i
        }
        return -1
    } else {
        return findAsciiRev(c, haystack, startIndex, endIndex)
    }
}

internal data class PrefilterAsciiResult(
    val start: Int,
    val greedyEnd: Int,
    val end: Int,
)

internal fun prefilterAscii(
    config: Config,
    haystack: ByteArray,
    needle: ByteArray,
    onlyGreedy: Boolean,
): PrefilterAsciiResult? {
    if (needle.isEmpty() || needle.size > haystack.size) return null
    if (config.ignoreCase) {
        val searchLimit = haystack.size - needle.size + 1
        val start = findAsciiIgnoreCase(needle[0], haystack, 0, searchLimit)
        if (start < 0) return null
        var greedyEnd = start + 1
        for (i in 1 until needle.size) {
            val c = needle[i]
            val idx = findAsciiIgnoreCase(c, haystack, greedyEnd, haystack.size)
            if (idx < 0) return null
            greedyEnd = idx + 1
        }
        if (onlyGreedy) {
            return PrefilterAsciiResult(start, greedyEnd, greedyEnd)
        } else {
            val lastIdx = findAsciiIgnoreCaseRev(needle[needle.size - 1], haystack, greedyEnd, haystack.size)
            val end = if (lastIdx >= 0) lastIdx + 1 else greedyEnd
            return PrefilterAsciiResult(start, greedyEnd, end)
        }
    } else {
        val searchLimit = haystack.size - needle.size + 1
        val start = findAscii(needle[0], haystack, 0, searchLimit)
        if (start < 0) return null
        var greedyEnd = start + 1
        for (i in 1 until needle.size) {
            val c = needle[i]
            val idx = findAscii(c, haystack, greedyEnd, haystack.size)
            if (idx < 0) return null
            greedyEnd = idx + 1
        }
        if (onlyGreedy) {
            return PrefilterAsciiResult(start, greedyEnd, greedyEnd)
        } else {
            val lastIdx = findAsciiRev(needle[needle.size - 1], haystack, greedyEnd, haystack.size)
            val end = if (lastIdx >= 0) lastIdx + 1 else greedyEnd
            return PrefilterAsciiResult(start, greedyEnd, end)
        }
    }
}

internal data class PrefilterNonAsciiResult(
    val start: Int,
    val end: Int,
)

internal fun prefilterNonAscii(
    config: Config,
    haystack: CharArray,
    needle: Utf32Str,
    onlyGreedy: Boolean,
): PrefilterNonAsciiResult? {
    if (needle.isEmpty() || needle.length > haystack.size) return null
    val firstNeedleChar = needle.get(0)
    val searchLimit = haystack.size - needle.length + 1
    var start = -1
    for (i in 0 until searchLimit) {
        if (normalize(haystack[i], config) == firstNeedleChar) {
            start = i
            break
        }
    }
    if (start < 0) return null

    val lastNeedleChar = needle.last()
    if (onlyGreedy) {
        if (haystack.size - start < needle.length) return null
        return PrefilterNonAsciiResult(start, start + 1)
    } else {
        var end = -1
        for (i in (haystack.size - 1) downTo (start + 1)) {
            if (normalize(haystack[i], config) == lastNeedleChar) {
                end = i + 1
                break
            }
        }
        if (end < 0 || end - start < needle.length) {
            return null
        }
        return PrefilterNonAsciiResult(start, end)
    }
}
