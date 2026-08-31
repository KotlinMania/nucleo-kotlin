// port-lint: source matcher/src/exact.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.chars.AsciiChar
import io.github.kotlinmania.nucleo.chars.charClass
import io.github.kotlinmania.nucleo.chars.charClassAndNormalize
import io.github.kotlinmania.nucleo.chars.normalize

internal fun substringMatch1Ascii(
    config: Config,
    haystack: ByteArray,
    c: Byte,
    indices: MutableList<Int>?,
): Int? {
    var maxScore = 0
    var maxPos = 0
    val b = c.toInt() and 0xFF
    if (config.ignoreCase && b in 0x61..0x7A) {
        val lower = c
        val upper = (b - 32).toByte()
        for (i in 0 until haystack.size) {
            val h = haystack[i]
            if (h == lower || h == upper) {
                val prevCharClass =
                    if (i > 0) {
                        AsciiChar(haystack[i - 1]).charClass(config)
                    } else {
                        config.initialCharClass
                    }
                val charClass = AsciiChar(h).charClass(config)
                val bonus = config.bonusFor(prevCharClass, charClass)
                val score = bonus * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH
                if (score > maxScore) {
                    maxPos = i
                    maxScore = score
                    if (bonus >= config.bonusBoundaryWhite) {
                        break
                    }
                }
            }
        }
    } else {
        val charClass = AsciiChar(c).charClass(config)
        for (i in 0 until haystack.size) {
            if (haystack[i] == c) {
                val prevCharClass =
                    if (i > 0) {
                        AsciiChar(haystack[i - 1]).charClass(config)
                    } else {
                        config.initialCharClass
                    }
                val bonus = config.bonusFor(prevCharClass, charClass)
                val score = bonus * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH
                if (score > maxScore) {
                    maxPos = i
                    maxScore = score
                    if (bonus >= config.bonusBoundaryWhite) {
                        break
                    }
                }
            }
        }
    }
    if (maxScore == 0) return null
    indices?.add(maxPos)
    return maxScore
}

internal fun substringMatchAscii(
    config: Config,
    haystack: ByteArray,
    needle: ByteArray,
    indices: MutableList<Int>?,
): Int? {
    var maxScore = 0
    var maxPos = 0
    val needleLen = needle.size

    if (config.ignoreCase) {
        var firstLetterPos = -1
        for (i in 0 until needleLen) {
            val b = needle[i].toInt() and 0xFF
            if (b in 0x61..0x7A) {
                firstLetterPos = i
                break
            }
        }

        if (firstLetterPos == 0) {
            val searchLimit = haystack.size - needleLen + 1
            val lower = needle[0]
            val upper = (needle[0].toInt() - 32).toByte()
            for (i in 0 until searchLimit) {
                val h = haystack[i]
                if (h == lower || h == upper) {
                    val prevCharClass =
                        if (i > 0) {
                            AsciiChar(haystack[i - 1]).charClass(config)
                        } else {
                            config.initialCharClass
                        }
                    val charClass = AsciiChar(h).charClass(config)
                    val bonus = config.bonusFor(prevCharClass, charClass)
                    val score = bonus * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH
                    if (score > maxScore) {
                        var matches = true
                        for (j in 1 until needleLen) {
                            val hj = AsciiChar(haystack[i + j]).normalize(config).byte
                            val nj = AsciiChar(needle[j]).normalize(config).byte
                            if (hj != nj) {
                                matches = false
                                break
                            }
                        }
                        if (matches) {
                            maxPos = i
                            maxScore = score
                            if (bonus >= config.bonusBoundaryWhite) {
                                break
                            }
                        }
                    }
                }
            }
            if (maxScore == 0) return null
        } else if (firstLetterPos > 0) {
            val prefix = needle.copyOfRange(0, firstLetterPos)
            val searchLimit = haystack.size - needleLen + 1
            for (i in 0 until searchLimit) {
                var prefixMatch = true
                for (j in 0 until firstLetterPos) {
                    if (haystack[i + j] != prefix[j]) {
                        prefixMatch = false
                        break
                    }
                }
                if (prefixMatch) {
                    val prevCharClass =
                        if (i > 0) {
                            AsciiChar(haystack[i - 1]).charClass(config)
                        } else {
                            config.initialCharClass
                        }
                    val charClass = AsciiChar(haystack[i]).charClass(config)
                    val bonus = config.bonusFor(prevCharClass, charClass)
                    val score = bonus * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH
                    if (score > maxScore) {
                        var matches = true
                        for (j in firstLetterPos until needleLen) {
                            val hj = AsciiChar(haystack[i + j]).normalize(config).byte
                            val nj = AsciiChar(needle[j]).normalize(config).byte
                            if (hj != nj) {
                                matches = false
                                break
                            }
                        }
                        if (matches) {
                            maxPos = i
                            maxScore = score
                            if (bonus >= config.bonusBoundaryWhite) {
                                break
                            }
                        }
                    }
                }
            }
            if (maxScore == 0) return null
        }
    }

    if (maxScore == 0) {
        val charClass = AsciiChar(needle[0]).charClass(config)
        val searchLimit = haystack.size - needleLen + 1
        for (i in 0 until searchLimit) {
            var matches = true
            for (j in 0 until needleLen) {
                if (haystack[i + j] != needle[j]) {
                    matches = false
                    break
                }
            }
            if (matches) {
                val prevCharClass =
                    if (i > 0) {
                        AsciiChar(haystack[i - 1]).charClass(config)
                    } else {
                        config.initialCharClass
                    }
                val bonus = config.bonusFor(prevCharClass, charClass)
                val score = bonus * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH
                if (score > maxScore) {
                    maxPos = i
                    maxScore = score
                    if (bonus >= config.bonusBoundaryWhite) {
                        break
                    }
                }
            }
        }
        if (maxScore == 0) return null
    }

    return calculateScoreAscii(config, haystack, needle, maxPos, maxPos + needleLen, indices)
}

internal fun substringMatch1NonAscii(
    config: Config,
    haystack: CharArray,
    needle: Char,
    start: Int,
    indices: MutableList<Int>?,
): Int {
    var maxScore = 0
    var maxPos = 0
    var prevClass =
        if (start > 0) {
            charClass(haystack[start - 1], config)
        } else {
            config.initialCharClass
        }
    for (i in start until haystack.size) {
        val (c, charClass) = charClassAndNormalize(haystack[i], config)
        if (c != needle) {
            prevClass = charClass
            continue
        }
        val bonus = config.bonusFor(prevClass, charClass)
        prevClass = charClass
        val score = bonus * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH
        if (score > maxScore) {
            maxPos = i - start
            maxScore = score
            if (bonus >= config.bonusBoundaryWhite) {
                break
            }
        }
    }
    indices?.add(maxPos + start)
    return maxScore
}

internal fun substringMatchNonAscii(
    config: Config,
    haystack: CharArray,
    needle: Utf32Str,
    start: Int,
    indices: MutableList<Int>?,
): Int? {
    var maxScore = 0
    var maxPos = 0
    var prevClass =
        if (start > 0) {
            charClass(haystack[start - 1], config)
        } else {
            config.initialCharClass
        }
    val needleLen = needle.length
    val end = haystack.size - needleLen
    val firstNeedleChar = needle.get(0)
    for (i in start..end) {
        val (c, charClass) = charClassAndNormalize(haystack[i], config)
        if (c != firstNeedleChar) {
            prevClass = charClass
            continue
        }
        val bonus = config.bonusFor(prevClass, charClass)
        prevClass = charClass
        val score = bonus * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH
        if (score > maxScore) {
            var matches = true
            for (j in 1 until needleLen) {
                val hj = normalize(haystack[i + j], config)
                val nj = needle.get(j)
                if (hj != nj) {
                    matches = false
                    break
                }
            }
            if (matches) {
                maxPos = i - start
                maxScore = score
                if (bonus >= config.bonusBoundaryWhite) {
                    break
                }
            }
        }
    }
    if (maxScore == 0) return null
    val needleChars = CharArray(needleLen) { needle.get(it) }
    return calculateScoreUnicode(config, haystack, needleChars, start + maxPos, start + maxPos + needleLen, indices)
}

internal fun substringMatchAsciiWithPrefilter(
    config: Config,
    haystack: ByteArray,
    needle: ByteArray,
    prefilterLen: Int,
    prefilter: Sequence<Int>,
): Pair<Int, Int> {
    val needleWithoutPrefilter = needle.copyOfRange(prefilterLen, needle.size)
    var maxScore = 0
    var maxPos = 0
    for (i in prefilter) {
        val prevCharClass =
            if (i > 0) {
                AsciiChar(haystack[i - 1]).charClass(config)
            } else {
                config.initialCharClass
            }
        val charClass = AsciiChar(haystack[i]).charClass(config)
        val bonus = config.bonusFor(prevCharClass, charClass)
        val score = bonus * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH
        val endIdx = minOf(i + needle.size, haystack.size)
        val haySub = haystack.copyOfRange(i + prefilterLen, endIdx)
        var matches = haySub.size == needleWithoutPrefilter.size
        if (matches) {
            for (idx in haySub.indices) {
                if (AsciiChar(haySub[idx]).normalize(config).byte != needleWithoutPrefilter[idx]) {
                    matches = false
                    break
                }
            }
        }
        if (score > maxScore && matches) {
            maxPos = i
            maxScore = score
            if (bonus >= config.bonusBoundaryWhite) {
                break
            }
        }
    }
    return Pair(maxScore, maxPos)
}

internal fun Matcher.substringMatch1Ascii(
    haystack: ByteArray,
    c: Byte,
    indices: MutableList<Int>? = null,
): Int? = substringMatch1Ascii(config, haystack, c, indices)

internal fun Matcher.substringMatchAsciiWithPrefilter(
    haystack: ByteArray,
    needle: ByteArray,
    prefilterLen: Int,
    prefilter: Sequence<Int>,
): Pair<Int, Int> = substringMatchAsciiWithPrefilter(config, haystack, needle, prefilterLen, prefilter)

internal fun Matcher.substringMatchAscii(
    haystack: ByteArray,
    needle: ByteArray,
    indices: MutableList<Int>? = null,
): Int? = substringMatchAscii(config, haystack, needle, indices)

internal fun Matcher.substringMatch1NonAscii(
    haystack: CharArray,
    needle: Char,
    start: Int,
    indices: MutableList<Int>? = null,
): Int = substringMatch1NonAscii(config, haystack, needle, start, indices)

internal fun Matcher.substringMatchNonAscii(
    haystack: CharArray,
    needle: Utf32Str,
    start: Int,
    indices: MutableList<Int>? = null,
): Int? = substringMatchNonAscii(config, haystack, needle, start, indices)
