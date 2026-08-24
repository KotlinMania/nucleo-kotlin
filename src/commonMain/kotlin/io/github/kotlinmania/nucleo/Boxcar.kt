// port-lint: source boxcar.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.Boxcar.MAX_ENTRIES
import io.github.kotlinmania.nucleo.Boxcar.SKIP
import io.github.kotlinmania.nucleo.Boxcar.SKIP_BUCKET

internal object Boxcar {
    const val SKIP: UInt = 32u
    val SKIP_BUCKET: Int = (32 - SKIP.countLeadingZeroBits()) - 1
    val BUCKETS: Int = 32 - SKIP_BUCKET
    const val MAX_ENTRIES: UInt = 0xFFFFFFDFu
}

internal data class Location(
    val bucket: Int,
    val bucketLen: Int,
    val entry: Int,
) {
    fun allocNextBucketEntry(): Int = bucketLen - (bucketLen shr 3)

    companion object {
        fun of(index: UInt): Location {
            val skipped = index + SKIP
            val leadingZeros = skipped.countLeadingZeroBits()
            val bucket = (32 - leadingZeros) - (SKIP_BUCKET + 1)
            val len = bucketLen(bucket)
            val entry = (skipped.toInt() xor len)
            return Location(bucket, len, entry)
        }

        fun bucketLen(bucket: Int): Int = 1 shl (bucket + SKIP_BUCKET)
    }
}

internal typealias SnapshotItem<T> = Pair<UInt, Item<T>?>

internal class Iter<T>(
    val location: Location,
    var idx: UInt,
    val end: UInt,
    val vec: BoxcarVec<T>,
) {
    fun end(): UInt = end
}

internal class Entry<T>(
    val item: Item<T>?,
)

internal class Bucket<T>(
    val entries: MutableList<Entry<T>>,
)

/**
 * A lock-free, append-only vector.
 */
internal class BoxcarVec<T>(
    private val columns: UInt,
) {
    private val entries = mutableListOf<Item<T>?>()

    fun columns(): UInt = columns

    fun count(): UInt = entries.size.toUInt().coerceAtMost(MAX_ENTRIES)

    fun getUnchecked(index: UInt): Item<T> = entries[index.toInt()]!!

    fun get(index: UInt): Item<T>? {
        val i = index.toInt()
        if (i < 0 || i >= entries.size) return null
        return entries[i]
    }

    fun push(value: T, fillColumns: (T, Array<Utf32String>) -> Unit): UInt {
        val cols = Array<Utf32String>(columns.toInt()) { Utf32String.empty() }
        fillColumns(value, cols)
        val item = Item(value, cols.toList())
        val idx = entries.size.toUInt()
        entries.add(item)
        return idx
    }

    fun extend(values: Iterable<T>, fillColumns: (T, Array<Utf32String>) -> Unit) {
        for (value in values) {
            push(value, fillColumns)
        }
    }

    fun snapshot(start: UInt): List<SnapshotItem<T>> {
        val end = count()
        val s = start.toInt().coerceIn(0, end.toInt())
        val e = end.toInt()
        val result = ArrayList<SnapshotItem<T>>(e - s)
        for (i in s until e) {
            result.add(i.toUInt() to entries[i])
        }
        return result
    }

    companion object {
        fun <T> withCapacity(capacity: UInt, columns: UInt): BoxcarVec<T> {
            require(columns > 0u) { "there must be atleast one matcher column" }
            return BoxcarVec(columns)
        }
    }
}

internal typealias Vec<T> = BoxcarVec<T>
