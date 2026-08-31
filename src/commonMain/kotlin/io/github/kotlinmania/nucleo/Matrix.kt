// port-lint: source matcher/src/matrix.rs
package io.github.kotlinmania.nucleo

internal const val MAX_MATRIX_SIZE: Int = 100 * 1024
internal const val MAX_HAYSTACK_LEN: Int = 2048
internal const val MAX_NEEDLE_LEN: Int = 2048

internal class MatrixLayout(
    public val haystackLen: Int,
    public val needleLen: Int,
    public val haystackOff: Int = 0,
    public val bonusOff: Int = 0,
    public val rowsOff: Int = 0,
    public val scoreOff: Int = 0,
    public val matrixOff: Int = 0,
) {
    public fun fiedsFromPtr(): Boolean = true

    public companion object {
        public fun new(haystackLen: Int, needleLen: Int): MatrixLayout {
            require(haystackLen >= needleLen)
            return MatrixLayout(
                haystackLen = haystackLen,
                needleLen = needleLen,
                haystackOff = 0,
                bonusOff = haystackLen,
                rowsOff = haystackLen + haystackLen,
                scoreOff = haystackLen + haystackLen + needleLen,
                matrixOff = haystackLen + haystackLen + needleLen + (haystackLen + 1 - needleLen),
            )
        }
    }
}

internal data class ScoreCell(
    public var score: Int = 0,
    public var consecutiveBonus: Int = 0,
    public var matched: Boolean = false,
) {
    public fun copyFrom(other: ScoreCell) {
        this.score = other.score
        this.consecutiveBonus = other.consecutiveBonus
        this.matched = other.matched
    }

    public fun set(score: Int, consecutiveBonus: Int, matched: Boolean) {
        this.score = score
        this.consecutiveBonus = consecutiveBonus
        this.matched = matched
    }
}

internal class MatcherDataView<C>(
    public val haystack: Array<C>,
    public val bonus: ByteArray,
    public val currentRow: Array<ScoreCell>,
    public val rowOffs: ShortArray,
    public val matrixCells: Array<MatrixCell>,
)

internal class MatrixCell(
    public var value: Byte = 0,
) {
    public fun set(pMatch: Boolean, mMatch: Boolean) {
        value = ((if (pMatch) 1 else 0) or (if (mMatch) 2 else 0)).toByte()
    }

    public fun get(mMatrix: Boolean): Boolean {
        val mask = if (mMatrix) 2 else 1
        return (value.toInt() and mask) != 0
    }
}

internal class MatcherData(
    public val haystack: CharArray = CharArray(MAX_HAYSTACK_LEN),
    public val bonus: ByteArray = ByteArray(MAX_HAYSTACK_LEN),
    public val rowOffs: ShortArray = ShortArray(MAX_NEEDLE_LEN),
    public val scratchSpace: Array<ScoreCell> = Array(MAX_HAYSTACK_LEN) { ScoreCell() },
    public val matrix: ByteArray = ByteArray(MAX_MATRIX_SIZE),
)

internal class MatrixSlab {
    internal val asciiHaystackBuf: ByteArray = ByteArray(MAX_HAYSTACK_LEN)
    internal val unicodeHaystackBuf: CharArray = CharArray(MAX_HAYSTACK_LEN)
    internal val bonusBuf: IntArray = IntArray(MAX_HAYSTACK_LEN)
    internal val rowOffsBuf: IntArray = IntArray(MAX_NEEDLE_LEN)
    internal val scoreCellsBuf: Array<ScoreCell> = Array(MAX_HAYSTACK_LEN) { ScoreCell() }
    internal val matrixCellsBuf: Array<MatrixCell> = Array(MAX_MATRIX_SIZE) { MatrixCell() }

    public fun alloc(haystackLen: Int, needleLen: Int): Boolean {
        val cells = haystackLen * needleLen
        return cells <= MAX_MATRIX_SIZE && haystackLen <= MAX_HAYSTACK_LEN && needleLen <= MAX_NEEDLE_LEN
    }

    public fun drop() {
        // In Kotlin, memory is managed by garbage collector
    }

    public companion object {
        public fun new(): MatrixSlab = MatrixSlab()
    }
}
