// port-lint: source matcher/src/fuzzy_optimal.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.chars.AsciiChar
import io.github.kotlinmania.nucleo.chars.charClass
import io.github.kotlinmania.nucleo.chars.charClassAndNormalize
import kotlin.math.max

private val UNMATCHED = ScoreCell(score = 0, consecutiveBonus = 0, matched = true)

private fun nextMCell(pScore: Int, bonus: Int, mCell: ScoreCell): ScoreCell {
    if (mCell.score == UNMATCHED.score && mCell.consecutiveBonus == UNMATCHED.consecutiveBonus && mCell.matched == UNMATCHED.matched) {
        return ScoreCell(
            score = pScore + bonus + SCORE_MATCH,
            matched = false,
            consecutiveBonus = bonus,
        )
    }

    var consecutiveBonus = max(mCell.consecutiveBonus, BONUS_CONSECUTIVE)
    if (bonus >= BONUS_BOUNDARY && bonus > consecutiveBonus) {
        consecutiveBonus = bonus
    }

    val scoreMatch = mCell.score + max(consecutiveBonus, bonus)
    val scoreSkip = pScore + bonus
    return if (scoreMatch > scoreSkip) {
        ScoreCell(
            score = scoreMatch + SCORE_MATCH,
            matched = true,
            consecutiveBonus = consecutiveBonus,
        )
    } else {
        ScoreCell(
            score = scoreSkip + SCORE_MATCH,
            matched = false,
            consecutiveBonus = bonus,
        )
    }
}

private fun pScore(prevPScore: Int, prevMScore: Int): Pair<Int, Boolean> {
    val scoreMatch = (prevMScore - PENALTY_GAP_START).coerceAtLeast(0)
    val scoreSkip = (prevPScore - PENALTY_GAP_EXTENSION).coerceAtLeast(0)
    return if (scoreMatch > scoreSkip) {
        Pair(scoreMatch, true)
    } else {
        Pair(scoreSkip, false)
    }
}

internal fun fuzzyMatchOptimalAscii(
    config: Config,
    haystack: ByteArray,
    needle: ByteArray,
    start: Int,
    greedyEnd: Int,
    end: Int,
    indices: MutableList<Int>?,
): Int? {
    val sliceLen = end - start
    if (sliceLen * needle.size > MAX_MATRIX_SIZE || sliceLen > MAX_HAYSTACK_LEN || needle.size > MAX_NEEDLE_LEN) {
        return fuzzyMatchGreedyAscii(config, haystack, needle, start, greedyEnd, indices)
    }

    val prevClass =
        if (start > 0) {
            AsciiChar(haystack[start - 1]).charClass(config)
        } else {
            config.initialCharClass
        }

    val slice = haystack.copyOfRange(start, end)
    val bonus = IntArray(sliceLen)
    val rowOffs = IntArray(needle.size)
    var prevClassIter = prevClass

    var needleIterIdx = 0
    var needleChar = AsciiChar(needle[needleIterIdx])
    var matched = false

    for (i in 0 until sliceLen) {
        val (c, clazz) = AsciiChar(slice[i]).charClassAndNormalize(config)
        slice[i] = c.byte
        val b = config.bonusFor(prevClassIter, clazz)
        bonus[i] = b
        prevClassIter = clazz

        if (c == needleChar) {
            needleIterIdx++
            if (needleIterIdx < needle.size) {
                rowOffs[needleIterIdx - 1] = i
                needleChar = AsciiChar(needle[needleIterIdx])
            } else if (!matched) {
                rowOffs[needleIterIdx - 1] = i
                matched = true
            }
        }
    }
    if (!matched) return null

    val currentRowWidth = sliceLen + 1 - needle.size
    val currentRow = Array(currentRowWidth) { ScoreCell() }
    val totalMatrixCells = currentRowWidth * needle.size
    val matrixCells = Array(totalMatrixCells) { MatrixCell() }

    var prefixBonus =
        if (config.preferPrefix) {
            if (start == 0) {
                MAX_PREFIX_BONUS * PREFIX_BONUS_SCALE
            } else {
                (MAX_PREFIX_BONUS * PREFIX_BONUS_SCALE - PENALTY_GAP_START).coerceAtLeast(0).saturatingSub(
                    minOf(start - 1, 65535) * PENALTY_GAP_EXTENSION,
                )
            }
        } else {
            0
        }

    scoreRowAscii(
        firstRow = true,
        recordIndices = indices != null,
        currentRow = currentRow,
        matrixCells = matrixCells,
        matrixCellsOffset = 0,
        haystack = slice,
        bonus = bonus,
        rowOff = 0,
        nextRowOff = rowOffs[1],
        needleIdx = 0,
        needleChar = AsciiChar(needle[0]),
        nextNeedleChar = AsciiChar(needle[1]),
        initialPrefixBonus = prefixBonus,
    )

    var matrixCellsOffset = currentRowWidth
    for (i in 1 until needle.size - 1) {
        val rowOff = rowOffs[i]
        val nextRowOff = rowOffs[i + 1]
        scoreRowAscii(
            firstRow = false,
            recordIndices = indices != null,
            currentRow = currentRow,
            matrixCells = matrixCells,
            matrixCellsOffset = matrixCellsOffset,
            haystack = slice,
            bonus = bonus,
            rowOff = rowOff,
            nextRowOff = nextRowOff,
            needleIdx = i,
            needleChar = AsciiChar(needle[i]),
            nextNeedleChar = AsciiChar(needle[i + 1]),
            initialPrefixBonus = 0,
        )
        val len = currentRowWidth + (i - 1) + 1 - rowOff
        matrixCellsOffset += len
    }

    val lastRowOff = rowOffs[needle.size - 1]
    val relativeLastRowOff = lastRowOff + 1 - needle.size
    var maxScore = -1
    var matchEnd = -1
    for (col in 0 until (currentRowWidth - relativeLastRowOff)) {
        val cell = currentRow[relativeLastRowOff + col]
        if (cell.score > maxScore) {
            maxScore = cell.score
            matchEnd = col
        }
    }

    if (indices != null && matchEnd >= 0) {
        reconstructOptimalPath(
            maxScoreEnd = matchEnd,
            indices = indices,
            matrixLen = matrixCellsOffset,
            start = start,
            rowOffs = rowOffs,
            currentRow = currentRow,
            matrixCells = matrixCells,
        )
    }

    return if (maxScore >= 0) maxScore else null
}

private fun scoreRowAscii(
    firstRow: Boolean,
    recordIndices: Boolean,
    currentRow: Array<ScoreCell>,
    matrixCells: Array<MatrixCell>,
    matrixCellsOffset: Int,
    haystack: ByteArray,
    bonus: IntArray,
    rowOff: Int,
    nextRowOff: Int,
    needleIdx: Int,
    needleChar: AsciiChar,
    nextNeedleChar: AsciiChar,
    initialPrefixBonus: Int,
) {
    val adjNextRowOff = nextRowOff - 1
    val relativeRowOff = rowOff - needleIdx
    val nextRelativeRowOff = adjNextRowOff - needleIdx

    var prevPScore = 0
    var prevMScore = 0
    var prefixBonus = initialPrefixBonus

    for (col in rowOff until adjNextRowOff) {
        val (pScore, pMatched) = pScore(prevPScore, prevMScore)
        val relativeCol = col - needleIdx
        val mCell =
            if (firstRow) {
                val cell =
                    if (AsciiChar(haystack[col]) == needleChar) {
                        ScoreCell(
                            score = bonus[col] * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH + prefixBonus / PREFIX_BONUS_SCALE,
                            matched = false,
                            consecutiveBonus = bonus[col],
                        )
                    } else {
                        UNMATCHED.copy()
                    }
                prefixBonus = prefixBonus.saturatingSub(PENALTY_GAP_EXTENSION)
                cell
            } else {
                currentRow[relativeCol].copy()
            }
        if (recordIndices) {
            matrixCells[matrixCellsOffset + (col - rowOff)].set(pMatched, mCell.matched)
        }
        prevPScore = pScore
        prevMScore = mCell.score
    }

    val numColIter = currentRow.size - nextRelativeRowOff
    for (k in 0 until numColIter) {
        val col = adjNextRowOff + k
        val (pScore, pMatched) = pScore(prevPScore, prevMScore)
        val relativeCol = nextRelativeRowOff + k
        val scoreCell = currentRow[relativeCol]
        val matrixCell = matrixCells[matrixCellsOffset + (nextRelativeRowOff - relativeRowOff) + k]

        val mCell =
            if (firstRow) {
                val cell =
                    if (AsciiChar(haystack[col]) == needleChar) {
                        ScoreCell(
                            score = bonus[col] * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH + prefixBonus / PREFIX_BONUS_SCALE,
                            matched = false,
                            consecutiveBonus = bonus[col],
                        )
                    } else {
                        UNMATCHED.copy()
                    }
                prefixBonus = prefixBonus.saturatingSub(PENALTY_GAP_EXTENSION)
                cell
            } else {
                scoreCell.copy()
            }

        if (AsciiChar(haystack[col + 1]) == nextNeedleChar) {
            scoreCell.copyFrom(nextMCell(pScore, bonus[col + 1], mCell))
        } else {
            scoreCell.copyFrom(UNMATCHED)
        }

        if (recordIndices) {
            matrixCell.set(pMatched, mCell.matched)
        }
        prevPScore = pScore
        prevMScore = mCell.score
    }
}

internal fun fuzzyMatchOptimalUnicode(
    config: Config,
    haystack: CharArray,
    needle: CharArray,
    start: Int,
    greedyEnd: Int,
    end: Int,
    indices: MutableList<Int>?,
): Int? {
    val sliceLen = end - start
    if (sliceLen * needle.size > MAX_MATRIX_SIZE || sliceLen > MAX_HAYSTACK_LEN || needle.size > MAX_NEEDLE_LEN) {
        return fuzzyMatchGreedyUnicode(config, haystack, needle, start, greedyEnd, indices)
    }

    val prevClass =
        if (start > 0) {
            charClass(haystack[start - 1], config)
        } else {
            config.initialCharClass
        }

    val slice = haystack.copyOfRange(start, end)
    val bonus = IntArray(sliceLen)
    val rowOffs = IntArray(needle.size)
    var prevClassIter = prevClass

    var needleIterIdx = 0
    var needleChar = needle[needleIterIdx]
    var matched = false

    for (i in 0 until sliceLen) {
        val (c, clazz) = charClassAndNormalize(slice[i], config)
        slice[i] = c
        val b = config.bonusFor(prevClassIter, clazz)
        bonus[i] = b
        prevClassIter = clazz

        if (c == needleChar) {
            needleIterIdx++
            if (needleIterIdx < needle.size) {
                rowOffs[needleIterIdx - 1] = i
                needleChar = needle[needleIterIdx]
            } else if (!matched) {
                rowOffs[needleIterIdx - 1] = i
                matched = true
            }
        }
    }
    if (!matched) return null

    val currentRowWidth = sliceLen + 1 - needle.size
    val currentRow = Array(currentRowWidth) { ScoreCell() }
    val totalMatrixCells = currentRowWidth * needle.size
    val matrixCells = Array(totalMatrixCells) { MatrixCell() }

    var prefixBonus =
        if (config.preferPrefix) {
            if (start == 0) {
                MAX_PREFIX_BONUS * PREFIX_BONUS_SCALE
            } else {
                (MAX_PREFIX_BONUS * PREFIX_BONUS_SCALE - PENALTY_GAP_START).coerceAtLeast(0).saturatingSub(
                    minOf(start - 1, 65535) * PENALTY_GAP_EXTENSION,
                )
            }
        } else {
            0
        }

    scoreRowUnicode(
        firstRow = true,
        recordIndices = indices != null,
        currentRow = currentRow,
        matrixCells = matrixCells,
        matrixCellsOffset = 0,
        haystack = slice,
        bonus = bonus,
        rowOff = 0,
        nextRowOff = rowOffs[1],
        needleIdx = 0,
        needleChar = needle[0],
        nextNeedleChar = needle[1],
        initialPrefixBonus = prefixBonus,
    )

    var matrixCellsOffset = currentRowWidth
    for (i in 1 until needle.size - 1) {
        val rowOff = rowOffs[i]
        val nextRowOff = rowOffs[i + 1]
        scoreRowUnicode(
            firstRow = false,
            recordIndices = indices != null,
            currentRow = currentRow,
            matrixCells = matrixCells,
            matrixCellsOffset = matrixCellsOffset,
            haystack = slice,
            bonus = bonus,
            rowOff = rowOff,
            nextRowOff = nextRowOff,
            needleIdx = i,
            needleChar = needle[i],
            nextNeedleChar = needle[i + 1],
            initialPrefixBonus = 0,
        )
        val len = currentRowWidth + (i - 1) + 1 - rowOff
        matrixCellsOffset += len
    }

    val lastRowOff = rowOffs[needle.size - 1]
    val relativeLastRowOff = lastRowOff + 1 - needle.size
    var maxScore = -1
    var matchEnd = -1
    for (col in 0 until (currentRowWidth - relativeLastRowOff)) {
        val cell = currentRow[relativeLastRowOff + col]
        if (cell.score > maxScore) {
            maxScore = cell.score
            matchEnd = col
        }
    }

    if (indices != null && matchEnd >= 0) {
        reconstructOptimalPath(
            maxScoreEnd = matchEnd,
            indices = indices,
            matrixLen = matrixCellsOffset,
            start = start,
            rowOffs = rowOffs,
            currentRow = currentRow,
            matrixCells = matrixCells,
        )
    }

    return if (maxScore >= 0) maxScore else null
}

private fun scoreRowUnicode(
    firstRow: Boolean,
    recordIndices: Boolean,
    currentRow: Array<ScoreCell>,
    matrixCells: Array<MatrixCell>,
    matrixCellsOffset: Int,
    haystack: CharArray,
    bonus: IntArray,
    rowOff: Int,
    nextRowOff: Int,
    needleIdx: Int,
    needleChar: Char,
    nextNeedleChar: Char,
    initialPrefixBonus: Int,
) {
    val adjNextRowOff = nextRowOff - 1
    val relativeRowOff = rowOff - needleIdx
    val nextRelativeRowOff = adjNextRowOff - needleIdx

    var prevPScore = 0
    var prevMScore = 0
    var prefixBonus = initialPrefixBonus

    for (col in rowOff until adjNextRowOff) {
        val (pScore, pMatched) = pScore(prevPScore, prevMScore)
        val relativeCol = col - needleIdx
        val mCell =
            if (firstRow) {
                val cell =
                    if (haystack[col] == needleChar) {
                        ScoreCell(
                            score = bonus[col] * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH + prefixBonus / PREFIX_BONUS_SCALE,
                            matched = false,
                            consecutiveBonus = bonus[col],
                        )
                    } else {
                        UNMATCHED.copy()
                    }
                prefixBonus = prefixBonus.saturatingSub(PENALTY_GAP_EXTENSION)
                cell
            } else {
                currentRow[relativeCol].copy()
            }
        if (recordIndices) {
            matrixCells[matrixCellsOffset + (col - rowOff)].set(pMatched, mCell.matched)
        }
        prevPScore = pScore
        prevMScore = mCell.score
    }

    val numColIter = currentRow.size - nextRelativeRowOff
    for (k in 0 until numColIter) {
        val col = adjNextRowOff + k
        val (pScore, pMatched) = pScore(prevPScore, prevMScore)
        val relativeCol = nextRelativeRowOff + k
        val scoreCell = currentRow[relativeCol]
        val matrixCell = matrixCells[matrixCellsOffset + (nextRelativeRowOff - relativeRowOff) + k]

        val mCell =
            if (firstRow) {
                val cell =
                    if (haystack[col] == needleChar) {
                        ScoreCell(
                            score = bonus[col] * BONUS_FIRST_CHAR_MULTIPLIER + SCORE_MATCH + prefixBonus / PREFIX_BONUS_SCALE,
                            matched = false,
                            consecutiveBonus = bonus[col],
                        )
                    } else {
                        UNMATCHED.copy()
                    }
                prefixBonus = prefixBonus.saturatingSub(PENALTY_GAP_EXTENSION)
                cell
            } else {
                scoreCell.copy()
            }

        if (haystack[col + 1] == nextNeedleChar) {
            scoreCell.copyFrom(nextMCell(pScore, bonus[col + 1], mCell))
        } else {
            scoreCell.copyFrom(UNMATCHED)
        }

        if (recordIndices) {
            matrixCell.set(pMatched, mCell.matched)
        }
        prevPScore = pScore
        prevMScore = mCell.score
    }
}

private fun reconstructOptimalPath(
    maxScoreEnd: Int,
    indices: MutableList<Int>,
    matrixLen: Int,
    start: Int,
    rowOffs: IntArray,
    currentRow: Array<ScoreCell>,
    matrixCells: Array<MatrixCell>,
) {
    val lastRowOff = rowOffs.last()
    val path = IntArray(rowOffs.size)
    path[rowOffs.size - 1] = start + maxScoreEnd + lastRowOff

    val width = currentRow.size
    var matrixEnd = matrixLen
    var rowIterIdx = rowOffs.size - 2

    var col = maxScoreEnd
    val relativeLastRowOff = lastRowOff + 1 - rowOffs.size
    var matched = currentRow[col + relativeLastRowOff].matched

    var currentRowIdx = rowIterIdx
    var currentRowOff = rowOffs[currentRowIdx]
    var rowRelativeOff = currentRowOff - currentRowIdx
    var rowLen = width - rowRelativeOff
    var rowStart = matrixEnd - rowLen

    col += lastRowOff - currentRowOff - 1

    while (true) {
        if (matched) {
            path[currentRowIdx] = start + col + currentRowOff
        }
        val nextMatched = matrixCells[rowStart + col].get(matched)
        if (matched) {
            rowIterIdx--
            if (rowIterIdx < 0) {
                break
            }
            matrixEnd -= rowLen
            val nextRowIdx = rowIterIdx
            val nextRowOff = rowOffs[nextRowIdx]
            col += currentRowOff - nextRowOff
            currentRowIdx = nextRowIdx
            currentRowOff = nextRowOff
            rowRelativeOff = currentRowOff - currentRowIdx
            rowLen = width - rowRelativeOff
            rowStart = matrixEnd - rowLen
        }
        col -= 1
        matched = nextMatched
    }

    for (p in path) {
        indices.add(p)
    }
}

internal fun Matcher.fuzzyMatchOptimal(
    haystack: ByteArray,
    needle: ByteArray,
    start: Int,
    greedyEnd: Int,
    end: Int,
    indices: MutableList<Int>? = null,
): Int? = fuzzyMatchOptimalAscii(config, haystack, needle, start, greedyEnd, end, indices)

internal fun Matcher.fuzzyMatchOptimal(
    haystack: CharArray,
    needle: CharArray,
    start: Int,
    greedyEnd: Int,
    end: Int,
    indices: MutableList<Int>? = null,
): Int? = fuzzyMatchOptimalUnicode(config, haystack, needle, start, greedyEnd, end, indices)

internal fun setup(
    haystack: ByteArray,
    needle: ByteArray,
    config: Config,
    start: Int,
): Boolean = haystack.isNotEmpty() && needle.isNotEmpty()

internal fun scoreRow(
    currentRow: Array<ScoreCell>,
    matrixCells: Array<MatrixCell>,
    haystack: ByteArray,
    bonus: IntArray,
    rowOff: Int,
    nextRowOff: Int,
    needleIdx: Int,
    needleChar: AsciiChar,
    nextNeedleChar: AsciiChar,
    initialPrefixBonus: Int,
) {
    scoreRowAscii(
        firstRow = needleIdx == 0,
        recordIndices = false,
        currentRow = currentRow,
        matrixCells = matrixCells,
        matrixCellsOffset = 0,
        haystack = haystack,
        bonus = bonus,
        rowOff = rowOff,
        nextRowOff = nextRowOff,
        needleIdx = needleIdx,
        needleChar = needleChar,
        nextNeedleChar = nextNeedleChar,
        initialPrefixBonus = initialPrefixBonus,
    )
}

internal fun populateMatrix(
    currentRow: Array<ScoreCell>,
    matrixCells: Array<MatrixCell>,
    haystack: ByteArray,
    needle: ByteArray,
    rowOffs: IntArray,
    bonus: IntArray,
): Int = currentRow.size * needle.size

