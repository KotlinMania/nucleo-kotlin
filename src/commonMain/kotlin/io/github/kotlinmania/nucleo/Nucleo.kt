// port-lint: source lib.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.pattern.MultiPattern
import io.github.kotlinmania.nucleo.pattern.Status as PatternStatus

/**
 * Single-abstract-method interface for notification callbacks from matcher workers.
 */
public fun interface MatcherNotifier {
    public fun notifyChanged()
}

/**
 * A fixed-size container for multi-column search fields.
 */
public class Columns internal constructor(
    internal val array: Array<Utf32String>,
) {
    public val size: Int get() = array.size

    public operator fun get(index: Int): Utf32String = array[index]

    public operator fun set(index: Int, value: Utf32String) {
        array[index] = value
    }
}

/**
 * Single-abstract-method interface for populating multi-column search fields from an item.
 */
public fun interface ColumnFiller<T> {
    public fun fill(value: T, columns: Columns)
}

/**
 * A match candidate stored in a [Nucleo] worker.
 */
public class Item<T> internal constructor(
    public val data: T,
    public val matcherColumns: List<Utf32String>,
)

/**
 * An item that was successfully matched by a [Nucleo] worker.
 */
public data class Match(
    public var score: Long,
    public var idx: UInt,
)

/**
 * The status of a [Nucleo] worker after a tick.
 */
public data class Status(
    /**
     * Whether the current snapshot has changed.
     */
    public var changed: Boolean,
    /**
     * Whether the matcher is still processing in the background.
     */
    public var running: Boolean,
)

/**
 * A snapshot representing the results of a [Nucleo] worker after finishing a tick.
 */
public class Snapshot<T> internal constructor(
    internal var itemCount: UInt = 0u,
    internal val matchesList: MutableList<Match> = mutableListOf(),
    internal val patternSnapshot: MultiPattern = MultiPattern(1),
    internal var itemsList: MutableList<Item<T>> = mutableListOf(),
) {
    /**
     * Returns the total number of items in this snapshot.
     */
    public fun itemCount(): UInt = itemCount

    /**
     * Returns the pattern which items were matched against.
     */
    public fun pattern(): MultiPattern = patternSnapshot

    /**
     * Returns the number of items that matched the pattern.
     */
    public fun matchedItemCount(): UInt = matchesList.size.toUInt()

    /**
     * Returns the items corresponding to the matches in this snapshot.
     */
    public fun matchedItems(range: IntRange = matchesList.indices): List<Item<T>> {
        if (matchesList.isEmpty()) return emptyList()
        val clampedStart = range.first.coerceIn(0, matchesList.size)
        val clampedEnd = (range.last + 1).coerceIn(clampedStart, matchesList.size)
        return matchesList.subList(clampedStart, clampedEnd).map { m ->
            itemsList[m.idx.toInt()]
        }
    }

    /**
     * Returns the item at the given index without bounds checking.
     */
    public fun getItemUnchecked(index: UInt): Item<T> =
        itemsList[index.toInt()]

    /**
     * Returns the item at the given index.
     */
    public fun getItem(index: UInt): Item<T>? =
        itemsList.getOrNull(index.toInt())

    /**
     * Returns the matches corresponding to this snapshot.
     */
    public fun matches(): List<Match> = matchesList

    /**
     * Returns the item corresponding to the [n]th match.
     */
    public fun getMatchedItem(n: UInt): Item<T>? {
        val m = matchesList.getOrNull(n.toInt()) ?: return null
        return getItem(m.idx)
    }

    internal fun clear(newItems: MutableList<Item<T>>) {
        itemCount = 0u
        matchesList.clear()
        itemsList = newItems
    }

    internal fun update(
        newItemCount: UInt,
        newMatches: List<Match>,
        newPattern: MultiPattern,
        newItems: MutableList<Item<T>>,
    ) {
        itemCount = newItemCount
        matchesList.clear()
        matchesList.addAll(newMatches)
        patternSnapshot.cloneFrom(newPattern)
        itemsList = newItems
    }
}

/**
 * Internal state for [Nucleo].
 */
internal enum class State {
    Init,
    Cleared,
    Fresh,
    ;

    fun matcherItemRefs(): Int =
        when (this) {
            Cleared -> 1
            Init, Fresh -> 2
        }

    fun canceled(): Boolean = this != Fresh

    fun cleared(): Boolean = this != Fresh
}

/**
 * A handle that allows adding new items to a [Nucleo] worker.
 */
public class Injector<T> internal constructor(
    internal val items: MutableList<Item<T>>,
    internal val cols: Int,
    internal val notify: MatcherNotifier,
    internal val parent: Nucleo<T>,
) : AutoCloseable {
    /**
     * Creates a copy of this injector handle.
     */
    public fun clone(): Injector<T> {
        val inj = Injector(items, cols, notify, parent)
        parent.addInjector(inj)
        return inj
    }

    /**
     * Appends an element to the list of matched items.
     */
    public fun push(value: T, fillColumns: ColumnFiller<T>): UInt {
        val columns = Columns(Array(cols) { Utf32String.empty() })
        fillColumns.fill(value, columns)
        val item = Item(value, columns.array.toList())
        items.add(item)
        val idx = (items.size - 1).toUInt()
        notify.notifyChanged()
        return idx
    }

    /**
     * Appends multiple elements to the list of matched items.
     */
    public fun extend(values: Iterable<T>, fillColumns: ColumnFiller<T>) {
        val columns = Columns(Array(cols) { Utf32String.empty() })
        for (value in values) {
            for (i in 0 until cols) {
                columns.array[i] = Utf32String.empty()
            }
            fillColumns.fill(value, columns)
            val item = Item(value, columns.array.toList())
            items.add(item)
        }
        notify.notifyChanged()
    }

    /**
     * Returns the total number of items injected in the matcher.
     */
    public fun injectedItems(): UInt = items.size.toUInt()

    /**
     * Returns the item at the given index without bounds checking.
     */
    public fun getUnchecked(index: UInt): Item<T> = items[index.toInt()]

    /**
     * Returns the item at the given index without bounds checking.
     */
    public fun getItemUnchecked(index: UInt): Item<T> = items[index.toInt()]

    /**
     * Returns the item at the given index.
     */
    public fun get(index: UInt): Item<T>? = items.getOrNull(index.toInt())

    /**
     * Detaches this injector from the parent nucleo instance.
     */
    override fun close() {
        parent.removeInjector(this)
    }
}

/**
 * A high-level matcher worker that computes matches.
 */
public class Nucleo<T>(
    public var config: Config = Config.DEFAULT,
    public val notify: MatcherNotifier = MatcherNotifier {},
    numThreads: Int? = null,
    public val columns: UInt = 1u,
) : AutoCloseable {
    private var items: MutableList<Item<T>> = mutableListOf()
    internal val injectors: MutableList<Injector<T>> = mutableListOf()
    private val snapshotInstance: Snapshot<T> = Snapshot(0u, mutableListOf(), MultiPattern(columns.toInt()), items)
    private var sortResultsFlag: Boolean = true
    private var reverseItemsFlag: Boolean = false
    private var lastSnapshotCount: UInt = 0u
    private var matcher: Matcher = Matcher(config)
    private var state: State = State.Init

    /**
     * The pattern matched by this matcher.
     */
    public val pattern: MultiPattern = MultiPattern(columns.toInt())

    /**
     * Constructs a new [Nucleo] instance.
     */
    public companion object {
        public fun <T> new(
            config: Config = Config.DEFAULT,
            notify: MatcherNotifier = MatcherNotifier {},
            numThreads: Int? = null,
            columns: UInt = 1u,
        ): Nucleo<T> = Nucleo(config, notify, numThreads, columns)
    }

    /**
     * Returns the total number of active injectors.
     */
    public fun activeInjectors(): Int = injectors.size

    /**
     * Returns a snapshot of the current matcher state.
     */
    public fun snapshot(): Snapshot<T> = snapshotInstance

    /**
     * Returns an injector that can be used for adding candidates to the matcher.
     */
    public fun injector(): Injector<T> {
        val inj = Injector(items, columns.toInt(), notify, this)
        injectors.add(inj)
        return inj
    }

    internal fun addInjector(injector: Injector<T>) {
        injectors.add(injector)
    }

    internal fun removeInjector(injector: Injector<T>) {
        injectors.remove(injector)
    }

    /**
     * Restarts the item stream.
     */
    public fun restart(clearSnapshot: Boolean) {
        items = mutableListOf()
        injectors.clear()
        lastSnapshotCount = 0u
        state = State.Cleared
        if (clearSnapshot) {
            snapshotInstance.clear(items)
        }
    }

    /**
     * Updates the internal configuration.
     */
    public fun updateConfig(config: Config) {
        this.config = config
        this.matcher.config = config
    }

    /**
     * Sets whether the matcher should sort search results by score after matching.
     */
    public fun sortResults(sortResults: Boolean) {
        this.sortResultsFlag = sortResults
    }

    /**
     * Sets whether the matcher should reverse the order of the input.
     */
    public fun reverseItems(reverseItems: Boolean) {
        this.reverseItemsFlag = reverseItems
    }

    /**
     * Executes a matching tick and updates the snapshot.
     */
    public fun tick(timeout: ULong = 0u): Status {
        val patternStatus = pattern.status()
        val canceled = patternStatus != PatternStatus.Unchanged || state.canceled()
        val res = tickInner(timeout, canceled, patternStatus)
        if (!canceled) {
            return res
        }
        state = State.Fresh
        val status2 = tickInner(timeout, false, PatternStatus.Unchanged)
        res.changed = res.changed || status2.changed
        res.running = status2.running
        return res
    }

    private fun tickInner(timeout: ULong, canceled: Boolean, status: PatternStatus): Status {
        timeout.hashCode()
        status.hashCode()
        val hasNewItems = items.size.toUInt() > lastSnapshotCount
        val patternChanged = canceled

        if (!hasNewItems && !patternChanged) {
            return Status(changed = false, running = false)
        }

        pattern.resetStatus()
        val currentItems = items
        val matches = mutableListOf<Match>()

        if (pattern.isEmpty()) {
            for (i in currentItems.indices) {
                matches.add(Match(0L, i.toUInt()))
            }
        } else {
            for (i in currentItems.indices) {
                val item = currentItems[i]
                val score = pattern.score(item.matcherColumns, matcher)
                if (score != null) {
                    matches.add(Match(score, i.toUInt()))
                }
            }
        }

        if (sortResultsFlag) {
            matches.sortWith { match1, match2 ->
                if (match1.score != match2.score) {
                    match2.score.compareTo(match1.score) // descending score
                } else {
                    val item1 = currentItems[match1.idx.toInt()]
                    val item2 = currentItems[match2.idx.toInt()]
                    val len1 = item1.matcherColumns.sumOf { it.length }
                    val len2 = item2.matcherColumns.sumOf { it.length }
                    if (len1 == len2) {
                        if (reverseItemsFlag) {
                            match2.idx.compareTo(match1.idx)
                        } else {
                            match1.idx.compareTo(match2.idx)
                        }
                    } else {
                        len1.compareTo(len2)
                    }
                }
            }
        } else {
            if (reverseItemsFlag) {
                matches.sortByDescending { it.idx }
            } else {
                matches.sortBy { it.idx }
            }
        }

        lastSnapshotCount = currentItems.size.toUInt()
        snapshotInstance.update(lastSnapshotCount, matches, pattern, currentItems)

        return Status(changed = true, running = false)
    }

    /**
     * Closes the nucleo instance and clears resources.
     */
    override fun close() {
        injectors.clear()
    }

    /**
     * Drops resources associated with this nucleo instance.
     */
    public fun drop() {
        close()
    }
}
