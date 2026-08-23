// port-lint: source matrix.rs
package io.github.kotlinmania.nucleo

public const val MAX_MATRIX_SIZE: Int = 100 * 1024
public const val MAX_HAYSTACK_LEN: Int = 2048
public const val MAX_NEEDLE_LEN: Int = 2048

public data class ScoreCell(
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

public class MatrixCell(
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

public class MatrixSlab {
    internal val asciiHaystackBuf: ByteArray = ByteArray(MAX_HAYSTACK_LEN)
    internal val unicodeHaystackBuf: CharArray = CharArray(MAX_HAYSTACK_LEN)
    internal val bonusBuf: IntArray = IntArray(MAX_HAYSTACK_LEN)
    internal val rowOffsBuf: IntArray = IntArray(MAX_NEEDLE_LEN)
    internal val scoreCellsBuf: Array<ScoreCell> = Array(MAX_HAYSTACK_LEN) { ScoreCell() }
    internal val matrixCellsBuf: Array<MatrixCell> = Array(MAX_MATRIX_SIZE) { MatrixCell() }
}
