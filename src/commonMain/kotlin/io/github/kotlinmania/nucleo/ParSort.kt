// port-lint: source par_sort.rs
package io.github.kotlinmania.nucleo

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

/**
 * Parallel quicksort.
 *
 * This implementation follows the same pattern-defeating quicksort contract as
 * the upstream implementation. Kotlin common source does not expose Rayon's
 * work-stealing primitives, so the implementation keeps the same cancellation
 * boundary and in-place ordering behavior with safe list operations.
 */

/** When dropped, copies from `src` into `dest`. */
private class CopyOnDrop<T>(
    private val src: T,
    private val dest: MutableList<T>,
    private val index: Int,
) {
    fun drop() {
        dest[index] = src
    }
}

/**
 * Shifts the first element to the right until it encounters a greater or equal
 * element.
 */
private fun <T> shiftHead(v: MutableList<T>, isLess: (T, T) -> Boolean) {
    val len = v.size
    if (len >= 2 && isLess(v[1], v[0])) {
        val tmp = v[0]
        var i = 1
        while (i < len && isLess(v[i], tmp)) {
            v[i - 1] = v[i]
            i += 1
        }
        v[i - 1] = tmp
    }
}

/**
 * Shifts the last element to the left until it encounters a smaller or equal
 * element.
 */
private fun <T> shiftTail(v: MutableList<T>, isLess: (T, T) -> Boolean) {
    val len = v.size
    if (len >= 2 && isLess(v[len - 1], v[len - 2])) {
        val tmp = v[len - 1]
        var i = len - 2
        while (i >= 0 && isLess(tmp, v[i])) {
            v[i + 1] = v[i]
            i -= 1
        }
        v[i + 1] = tmp
    }
}

/**
 * Partially sorts a slice by shifting several out-of-order elements around.
 *
 * Returns `true` if the slice is sorted at the end. This function is
 * *O*(*n*) worst-case.
 */
private fun <T> partialInsertionSort(v: MutableList<T>, isLess: (T, T) -> Boolean): Boolean {
    val maxSteps = 5
    val shortestShifting = 50

    val len = v.size
    var i = 1

    repeat(maxSteps) {
        while (i < len && !isLess(v[i], v[i - 1])) {
            i += 1
        }

        if (i == len) {
            return true
        }

        if (len < shortestShifting) {
            return false
        }

        v.swap(i - 1, i)
        shiftTail(v.subList(0, i), isLess)
        shiftHead(v.subList(i, len), isLess)
    }

    return false
}

/** Sorts a slice using insertion sort, which is *O*(*n*^2) worst-case. */
private fun <T> insertionSort(v: MutableList<T>, isLess: (T, T) -> Boolean) {
    for (i in 1 until v.size) {
        shiftTail(v.subList(0, i + 1), isLess)
    }
}

/** Sorts `v` using heapsort, which guarantees *O*(*n* * log(*n*)) worst-case. */
private fun <T> heapSort(v: MutableList<T>, isLess: (T, T) -> Boolean) {
    fun siftDown(end: Int, start: Int) {
        var node = start
        while (true) {
            var child = 2 * node + 1
            if (child >= end) {
                break
            }

            if (child + 1 < end && isLess(v[child], v[child + 1])) {
                child += 1
            }

            if (!isLess(v[node], v[child])) {
                break
            }

            v.swap(node, child)
            node = child
        }
    }

    for (i in (v.size / 2 - 1) downTo 0) {
        siftDown(v.size, i)
    }

    for (i in v.size - 1 downTo 1) {
        v.swap(0, i)
        siftDown(i, 0)
    }
}

/**
 * Partitions `v` into elements smaller than `pivot`, followed by elements
 * greater than or equal to `pivot`.
 *
 * Returns the number of elements smaller than `pivot`.
 */
private fun <T> partitionInBlocks(
    v: MutableList<T>,
    pivot: T,
    isLess: (T, T) -> Boolean,
): PartitionInBlocksResult {
    var left = 0
    var right = v.size
    var wasPartitioned = true

    /** Returns the number of elements between indices `left` inclusive and `right` exclusive. */
    fun width(left: Int, right: Int): Int = right - left

    while (true) {
        while (left < right && isLess(v[left], pivot)) {
            left += 1
        }
        while (left < right && !isLess(v[right - 1], pivot)) {
            right -= 1
        }
        if (width(left, right) <= 0) {
            return PartitionInBlocksResult(left, wasPartitioned)
        }

        right -= 1
        v.swap(left, right)
        wasPartitioned = false
        left += 1
    }
}

/**
 * Partitions `v` into elements smaller than `v[pivot]`, followed by elements
 * greater than or equal to `v[pivot]`.
 *
 * Returns the number of elements smaller than `v[pivot]` and whether `v` was
 * already partitioned.
 */
private fun <T> partition(
    v: MutableList<T>,
    pivot: Int,
    isLess: (T, T) -> Boolean,
): PartitionResult {
    v.swap(0, pivot)
    val pivotValue = v[0]
    val pivotGuard = CopyOnDrop(pivotValue, v, 0)
    val result =
        try {
            val rest = v.subList(1, v.size)
            partitionInBlocks(rest, pivotValue, isLess)
        } finally {
            pivotGuard.drop()
        }
    val mid = result.mid
    v.swap(0, mid)
    return PartitionResult(mid, result.wasPartitioned)
}

/**
 * Partitions `v` into elements equal to `v[pivot]` followed by elements greater
 * than `v[pivot]`.
 *
 * Returns the number of elements equal to the pivot. It is assumed that `v` does
 * not contain elements smaller than the pivot.
 */
private fun <T> partitionEqual(v: MutableList<T>, pivot: Int, isLess: (T, T) -> Boolean): Int {
    v.swap(0, pivot)
    val pivotValue = v[0]
    val pivotGuard = CopyOnDrop(pivotValue, v, 0)

    var left = 1
    var right = v.size
    try {
        while (true) {
            while (left < right && !isLess(pivotValue, v[left])) {
                left += 1
            }
            while (left < right && isLess(pivotValue, v[right - 1])) {
                right -= 1
            }
            if (left >= right) {
                break
            }

            right -= 1
            v.swap(left, right)
            left += 1
        }
    } finally {
        pivotGuard.drop()
    }

    v.swap(0, left - 1)
    return left
}

/**
 * Scatters some elements around in an attempt to break patterns that might
 * cause imbalanced partitions in quicksort.
 */
private fun <T> breakPatterns(v: MutableList<T>) {
    val len = v.size
    if (len >= 8) {
        var random = len
        fun genInt(): Int {
            random = random xor (random shl 13)
            random = random xor (random ushr 17)
            random = random xor (random shl 5)
            return random
        }

        val modulus = len.takeHighestOneBit().let { highest ->
            if (highest == len) highest else highest shl 1
        }
        val pos = len / 4 * 2

        for (i in 0 until 3) {
            var other = genInt() and (modulus - 1)
            if (other >= len) {
                other -= len
            }

            v.swap(pos - 1 + i, other)
        }
    }
}

/**
 * Chooses a pivot in `v` and returns the index and `true` if the slice is
 * likely already sorted.
 *
 * Elements in `v` might be reordered in the process.
 */
private fun <T> choosePivot(v: MutableList<T>, isLess: (T, T) -> Boolean): PivotChoice {
    val shortestMedianOfMedians = 50
    val maxSwaps = 4 * 3

    val len = v.size
    var a = len / 4
    var b = len / 4 * 2
    var c = len / 4 * 3
    var swaps = 0

    fun sort2(first: Int, second: Int): OrderedPair {
        if (isLess(v[second], v[first])) {
            swaps += 1
            return OrderedPair(second, first)
        }
        return OrderedPair(first, second)
    }

    fun sort3(first: Int, second: Int, third: Int): OrderedTriple {
        val ab = sort2(first, second)
        val bc = sort2(ab.second, third)
        val abAgain = sort2(ab.first, bc.first)
        return OrderedTriple(abAgain.first, abAgain.second, bc.second)
    }

    if (len >= 8) {
        if (len >= shortestMedianOfMedians) {
            a = sort3(a - 1, a, a + 1).second
            b = sort3(b - 1, b, b + 1).second
            c = sort3(c - 1, c, c + 1).second
        }

        b = sort3(a, b, c).second
    }

    return if (swaps < maxSwaps) {
        PivotChoice(b, swaps == 0)
    } else {
        v.reverse()
        PivotChoice(len - 1 - b, true)
    }
}

/**
 * Sorts `v` recursively.
 *
 * If the slice had a predecessor in the original array, it is specified as
 * `pred`.
 *
 * `limit` is the number of allowed imbalanced partitions before switching to
 * heapsort. If zero, this function will immediately switch to heapsort.
 */
private fun <T> recurse(
    v: MutableList<T>,
    isLess: (T, T) -> Boolean,
    pred: T?,
    limit: Int,
    canceled: AtomicBoolean,
): Boolean {
    val maxInsertion = 20
    val maxSequential = 2_000

    var current = v
    var predecessor = pred
    var remainingLimit = limit
    var wasBalanced = true
    var wasPartitioned = true

    while (true) {
        val len = current.size

        if (len <= maxInsertion) {
            insertionSort(current, isLess)
            return false
        }

        if (remainingLimit == 0) {
            heapSort(current, isLess)
            return false
        }

        if (!wasBalanced) {
            breakPatterns(current)
            remainingLimit -= 1
        }

        val pivotChoice = choosePivot(current, isLess)
        val pivot = pivotChoice.index

        if (wasBalanced && wasPartitioned && pivotChoice.likelySorted) {
            if (partialInsertionSort(current, isLess)) {
                return false
            }
        }

        if (predecessor != null && !isLess(predecessor, current[pivot])) {
            val mid = partitionEqual(current, pivot, isLess)
            current = current.subList(mid, current.size)
            continue
        }

        val partition = partition(current, pivot, isLess)
        val mid = partition.mid
        wasBalanced = min(mid, len - mid) >= len / 8
        wasPartitioned = partition.wasPartitioned

        val left = current.subList(0, mid)
        val pivotValue = current[mid]
        val right = current.subList(mid + 1, current.size)

        if (max(left.size, right.size) <= maxSequential) {
            if (left.size < right.size) {
                recurse(left, isLess, predecessor, remainingLimit, canceled)
                current = right
                predecessor = pivotValue
            } else {
                recurse(right, isLess, pivotValue, remainingLimit, canceled)
                current = left
            }
        } else if (canceled.load()) {
            return true
        } else {
            val leftCanceled = recurse(left, isLess, predecessor, remainingLimit, canceled)
            val rightCanceled = recurse(right, isLess, pivotValue, remainingLimit, canceled)
            return leftCanceled || rightCanceled
        }
    }
}

/**
 * Sorts `v` using pattern-defeating quicksort in parallel.
 *
 * The algorithm is unstable, in-place, and *O*(*n* * log(*n*)) worst-case.
 */
internal fun <T> parQuicksort(
    v: MutableList<T>,
    isLess: (T, T) -> Boolean,
    canceled: AtomicBoolean,
): Boolean {
    if (canceled.load()) {
        return true
    }
    if (v.size < 2) {
        return false
    }

    val limit = Int.SIZE_BITS - v.size.countLeadingZeroBits()
    return recurse(v, isLess, null, limit, canceled)
}

private fun <T> MutableList<T>.swap(first: Int, second: Int) {
    if (first == second) {
        return
    }
    val tmp = this[first]
    this[first] = this[second]
    this[second] = tmp
}

private fun Int.takeHighestOneBit(): Int = 1 shl (Int.SIZE_BITS - 1 - countLeadingZeroBits())

private data class PartitionInBlocksResult(
    val mid: Int,
    val wasPartitioned: Boolean,
)

private data class PartitionResult(
    val mid: Int,
    val wasPartitioned: Boolean,
)

private data class PivotChoice(
    val index: Int,
    val likelySorted: Boolean,
)

private data class OrderedPair(
    val first: Int,
    val second: Int,
)

private data class OrderedTriple(
    val first: Int,
    val second: Int,
    val third: Int,
)
