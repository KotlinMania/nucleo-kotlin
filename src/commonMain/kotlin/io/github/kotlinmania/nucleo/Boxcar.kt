// port-lint: source boxcar.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.Boxcar.MAX_ENTRIES
import io.github.kotlinmania.nucleo.Boxcar.SKIP
import io.github.kotlinmania.nucleo.Boxcar.SKIP_BUCKET

internal object Boxcar {
    /**
     * Skip the shorter buckets to avoid unnecessary allocations.
     * This also reduces the maximum capacity of a vector.
     */
    const val SKIP: UInt = 32u
    val SKIP_BUCKET: Int = (32 - SKIP.countLeadingZeroBits()) - 1
    val BUCKETS: Int = 32 - SKIP_BUCKET
    const val MAX_ENTRIES: UInt = 0xFFFFFFDFu
}

/**
 * Calculates bucket index and offset for a given flat index.
 */
internal data class Location(
    /** The index of the bucket */
    val bucket: Int,
    /** The length of the bucket */
    val bucketLen: Int,
    /** The index of the entry in the bucket */
    val entry: Int,
) {
    /**
     * The entry index at which the next bucket should be pre-allocated.
     */
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

/**
 * An iterator over snapshot elements.
 */
internal class Iter<T>(
    val location: Location,
    var idx: UInt,
    val end: UInt,
    val vec: BoxcarVec<T>,
) : Iterator<SnapshotItem<T>> {
    fun end(): UInt = end

    override fun hasNext(): Boolean = idx < end

    override fun next(): SnapshotItem<T> {
        val curr = idx
        idx++
        return curr to vec.get(curr)
    }
}

/**
 * A parallel-compatible snapshot iterator.
 */
internal class ParIter<T>(
    val start: UInt,
    val end: UInt,
    val vec: BoxcarVec<T>,
) {
    fun optLen(): UInt = end - start

    fun len(): UInt = end - start

    fun splitAt(index: UInt): Pair<ParIter<T>, ParIter<T>> {
        val mid = (start + index).coerceAtMost(end)
        return ParIter(start, mid, vec) to ParIter(mid, end, vec)
    }
}

/**
 * An entry containing the item and column storage.
 */
internal class Entry<T>(
    val item: Item<T>?,
) {
    companion object {
        fun layout(cols: UInt): Int = 1 + cols.toInt()

        fun <T> matcherColsRaw(entry: Entry<T>, cols: UInt): List<Utf32String> =
            entry.item?.matcherColumns ?: emptyList()

        fun <T> matcherColsMut(entry: Entry<T>, cols: UInt): List<Utf32String> =
            entry.item?.matcherColumns ?: emptyList()

        fun <T> read(entry: Entry<T>, cols: UInt): Item<T>? = entry.item
    }
}

/**
 * A chunk/bucket of entries.
 */
internal class Bucket<T>(
    val entries: MutableList<Entry<T>>,
) {
    companion object {
        fun layout(len: UInt, cols: UInt): Int = len.toInt() * Entry.layout(cols)

        fun <T> alloc(len: UInt, cols: UInt): Bucket<T> =
            Bucket(ArrayList(len.toInt()))

        fun <T> dealloc(bucket: Bucket<T>, len: UInt, cols: UInt) {
            bucket.entries.clear()
        }

        fun <T> get(bucket: Bucket<T>, idx: UInt, cols: UInt): Entry<T>? =
            bucket.entries.getOrNull(idx.toInt())

        fun <T> new(entries: MutableList<Entry<T>>): Bucket<T> = Bucket(entries)
    }
}

/**
 * A lock-free, append-only vector for matcher candidates.
 */
internal class BoxcarVec<T>(
    private val columns: UInt,
) {
    private val entries = mutableListOf<Item<T>?>()

    /**
     * Returns the number of matcher columns configured in this vector.
     */
    fun columns(): UInt = columns

    /**
     * Returns the number of elements in the vector.
     */
    fun count(): UInt = entries.size.toUInt().coerceAtMost(MAX_ENTRIES)

    /**
     * Returns the item at [index] without bounds checking.
     */
    fun getUnchecked(index: UInt): Item<T> = entries[index.toInt()]!!

    /**
     * Returns the item at [index] if it exists, or null.
     */
    fun get(index: UInt): Item<T>? {
        val i = index.toInt()
        if (i < 0 || i >= entries.size) return null
        return entries[i]
    }

    /**
     * Appends an element to the back of the vector.
     */
    fun push(value: T, fillColumns: (T, Array<Utf32String>) -> Unit): UInt {
        val cols = Array<Utf32String>(columns.toInt()) { Utf32String.empty() }
        fillColumns(value, cols)
        val item = Item(value, cols.toList())
        val idx = entries.size.toUInt()
        entries.add(item)
        return idx
    }

    /**
     * Extends the vector by appending multiple elements at once.
     */
    fun extend(values: Iterable<T>, fillColumns: (T, Array<Utf32String>) -> Unit) {
        for (value in values) {
            push(value, fillColumns)
        }
    }

    /**
     * Returns a snapshot view of items starting from [start].
     */
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

    /**
     * Returns a parallel iterator over items starting from [start].
     */
    fun parSnapshot(start: UInt): ParIter<T> {
        val end = count()
        require(start <= end) { "index $start is out of bounds!" }
        return ParIter(start, end, this)
    }

    companion object {
        /**
         * Constructs a new, empty vector with the specified capacity and matcher columns.
         */
        fun <T> withCapacity(capacity: UInt, columns: UInt): BoxcarVec<T> {
            require(columns > 0u) { "there must be atleast one matcher column" }
            return BoxcarVec(columns)
        }

        fun <T> getOrAlloc(bucket: Bucket<T>, len: UInt, cols: UInt): Bucket<T> = bucket
    }
}

internal typealias Vec<T> = BoxcarVec<T>
