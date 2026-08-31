// port-lint: source matcher/src/score.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.chars.AsciiChar
import io.github.kotlinmania.nucleo.chars.charClass
import io.github.kotlinmania.nucleo.chars.charClassAndNormalize
import kotlin.math.max

public const val SCORE_MATCH: Int = 16
public const val PENALTY_GAP_START: Int = 3
public const val PENALTY_GAP_EXTENSION: Int = 1
public const val BONUS_BOUNDARY: Int = 8
public const val BONUS_CAMEL123: Int = 7
public const val BONUS_NON_WORD: Int = 6
public const val BONUS_CONSECUTIVE: Int = PENALTY_GAP_START + PENALTY_GAP_EXTENSION
public const val BONUS_FIRST_CHAR_MULTIPLIER: Int = 2
public const val MAX_PREFIX_BONUS: Int = 8
public const val PREFIX_BONUS_SCALE: Int = 2

internal fun Int.saturatingSub(penalty: Int): Int = max(0, this - penalty)

internal fun calculateScoreAscii(
    config: Config,
    haystack: ByteArray,
    needle: ByteArray,
    start: Int,
    end: Int,
    indices: MutableList<Int>?,
): Int {
    var prevClass =
        if (start > 0) {
            AsciiChar(haystack[start - 1]).charClass(config)
        } else {
            config.initialCharClass
        }
    var needleIdx = 0
    var needleChar = AsciiChar(needle[needleIdx])

    var inGap = false
    var consecutive = 1

    indices?.add(start)
    val clazz = AsciiChar(haystack[start]).charClass(config)
    var firstBonus = config.bonusFor(prevClass, clazz)
    var score = SCORE_MATCH + firstBonus * BONUS_FIRST_CHAR_MULTIPLIER
    prevClass = clazz
    needleIdx++
    if (needleIdx < needle.size) {
        needleChar = AsciiChar(needle[needleIdx])
    }

    for (i in (start + 1) until end) {
        val (c, charClass) = AsciiChar(haystack[i]).charClassAndNormalize(config)
        if (c == needleChar) {
            indices?.add(i)
            var bonus = config.bonusFor(prevClass, charClass)
            if (consecutive != 0) {
                if (bonus >= BONUS_BOUNDARY && bonus > firstBonus) {
                    firstBonus = bonus
                }
                bonus = max(max(bonus, firstBonus), BONUS_CONSECUTIVE)
            } else {
                firstBonus = bonus
            }
            score += SCORE_MATCH + bonus
            inGap = false
            consecutive += 1
            needleIdx++
            if (needleIdx < needle.size) {
                needleChar = AsciiChar(needle[needleIdx])
            }
        } else {
            val penalty = if (inGap) PENALTY_GAP_EXTENSION else PENALTY_GAP_START
            score = score.saturatingSub(penalty)
            inGap = true
            consecutive = 0
        }
        prevClass = charClass
    }

    if (config.preferPrefix) {
        if (start != 0) {
            val penalty = PENALTY_GAP_START + PENALTY_GAP_START * minOf(start - 1, 65535)
            score += MAX_PREFIX_BONUS.saturatingSub(penalty / PREFIX_BONUS_SCALE)
        } else {
            score += MAX_PREFIX_BONUS
        }
    }
    return score
}

internal fun calculateScoreUnicode(
    config: Config,
    haystack: CharArray,
    needle: CharArray,
    start: Int,
    end: Int,
    indices: MutableList<Int>?,
): Int {
    var prevClass =
        if (start > 0) {
            charClass(haystack[start - 1], config)
        } else {
            config.initialCharClass
        }
    var needleIdx = 0
    var needleChar = needle[needleIdx]

    var inGap = false
    var consecutive = 1

    indices?.add(start)
    val clazz = charClass(haystack[start], config)
    var firstBonus = config.bonusFor(prevClass, clazz)
    var score = SCORE_MATCH + firstBonus * BONUS_FIRST_CHAR_MULTIPLIER
    prevClass = clazz
    needleIdx++
    if (needleIdx < needle.size) {
        needleChar = needle[needleIdx]
    }

    for (i in (start + 1) until end) {
        val (c, charClass) = charClassAndNormalize(haystack[i], config)
        if (c == needleChar) {
            indices?.add(i)
            var bonus = config.bonusFor(prevClass, charClass)
            if (consecutive != 0) {
                if (bonus >= BONUS_BOUNDARY && bonus > firstBonus) {
                    firstBonus = bonus
                }
                bonus = max(max(bonus, firstBonus), BONUS_CONSECUTIVE)
            } else {
                firstBonus = bonus
            }
            score += SCORE_MATCH + bonus
            inGap = false
            consecutive += 1
            needleIdx++
            if (needleIdx < needle.size) {
                needleChar = needle[needleIdx]
            }
        } else {
            val penalty = if (inGap) PENALTY_GAP_EXTENSION else PENALTY_GAP_START
            score = score.saturatingSub(penalty)
            inGap = true
            consecutive = 0
        }
        prevClass = charClass
    }

    if (config.preferPrefix) {
        if (start != 0) {
            val penalty = PENALTY_GAP_START + PENALTY_GAP_START * minOf(start - 1, 65535)
            score += MAX_PREFIX_BONUS.saturatingSub(penalty / PREFIX_BONUS_SCALE)
        } else {
            score += MAX_PREFIX_BONUS
        }
    }
    return score
}

internal fun Matcher.bonusFor(prevClass: io.github.kotlinmania.nucleo.chars.CharClass, clazz: io.github.kotlinmania.nucleo.chars.CharClass): Int =
    config.bonusFor(prevClass, clazz)

internal fun Matcher.calculateScore(
    haystack: ByteArray,
    needle: ByteArray,
    start: Int,
    end: Int,
    indices: MutableList<Int>? = null,
): Int = calculateScoreAscii(config, haystack, needle, start, end, indices)

internal fun Matcher.calculateScore(
    haystack: CharArray,
    needle: CharArray,
    start: Int,
    end: Int,
    indices: MutableList<Int>? = null,
): Int = calculateScoreUnicode(config, haystack, needle, start, end, indices)
