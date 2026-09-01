// port-lint: source src/worker.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.pattern.MultiPattern
import io.github.kotlinmania.nucleo.pattern.Status as PatternStatus

internal class Matchers(
    private val matchers: List<Matcher>,
) {
    fun get(): Matcher = matchers.firstOrNull() ?: Matcher()

    companion object {
        fun create(count: Int, config: Config): Matchers =
            Matchers(List(count.coerceAtLeast(1)) { Matcher(config) })
    }
}

internal class Worker<T>(
    var config: Config,
    var notify: () -> Unit,
    val cols: UInt,
) {
    var running: Boolean = false
    private var matchers: Matchers = Matchers.create(1, config)
    val matches: MutableList<Match> = mutableListOf()
    val pattern: MultiPattern = MultiPattern(cols.toInt())
    var sortResults: Boolean = true
    var reverseItems: Boolean = false
    var canceled: Boolean = false
    var shouldNotify: Boolean = false
    var wasCanceled: Boolean = false
    var lastSnapshot: UInt = 0u
    var items: BoxcarVec<T> = BoxcarVec.withCapacity(2048u, cols)
    val inFlight: MutableList<UInt> = mutableListOf()

    fun itemCount(): UInt =
        if (lastSnapshot >= inFlight.size.toUInt()) {
            lastSnapshot - inFlight.size.toUInt()
        } else {
            0u
        }

    fun updateConfig(config: Config) {
        this.config = config
        this.matchers = Matchers.create(1, config)
    }

    fun sortResults(sortResults: Boolean) {
        this.sortResults = sortResults
    }

    fun reverseItems(reverseItems: Boolean) {
        this.reverseItems = reverseItems
    }

    fun removeInFlightMatches() {
        var off = 0
        val remaining = mutableListOf<UInt>()
        for (i in inFlight) {
            val isInFlight = items.get(i) == null
            if (isInFlight) {
                val removeIdx = (i.toInt() - off).coerceIn(0, matches.size)
                if (removeIdx < matches.size) {
                    matches.removeAt(removeIdx)
                    off++
                }
                remaining.add(i)
            }
        }
        inFlight.clear()
        inFlight.addAll(remaining)
    }

    fun resetMatches() {
        matches.clear()
        for (idx in 0u until lastSnapshot) {
            matches.add(Match(0L, idx))
        }
        removeInFlightMatches()
    }

    fun processNewItemsTrivial() {
        val newSnapshot = items.snapshot(lastSnapshot)
        if (newSnapshot.isNotEmpty()) {
            for ((idx, item) in newSnapshot) {
                if (item == null) {
                    inFlight.add(idx)
                } else {
                    matches.add(Match(0L, idx))
                }
            }
            lastSnapshot = (newSnapshot.last().first + 1u)
        }
    }

    fun processNewItems(unmatchedCounter: IntArray) {
        val currentMatcher = matchers.get()
        val remainingInFlight = mutableListOf<UInt>()
        for (idx in inFlight) {
            val item = items.get(idx)
            if (item == null) {
                remainingInFlight.add(idx)
            } else {
                val score = pattern.score(item.matcherColumns, currentMatcher)
                if (score != null) {
                    matches.add(Match(score, idx))
                }
            }
        }
        inFlight.clear()
        inFlight.addAll(remainingInFlight)

        val newSnapshot = items.snapshot(lastSnapshot)
        if (newSnapshot.isNotEmpty()) {
            for ((idx, item) in newSnapshot) {
                if (item == null) {
                    inFlight.add(idx)
                    unmatchedCounter[0]++
                    matches.add(Match(0L, UInt.MAX_VALUE))
                } else if (canceled) {
                    matches.add(Match(0L, idx))
                } else {
                    val score = pattern.score(item.matcherColumns, currentMatcher)
                    if (score == null) {
                        unmatchedCounter[0]++
                        matches.add(Match(0L, UInt.MAX_VALUE))
                    } else {
                        matches.add(Match(score, idx))
                    }
                }
            }
            lastSnapshot = (newSnapshot.last().first + 1u)
        }
    }

    fun run(patternStatus: PatternStatus, cleared: Boolean) {
        running = true
        wasCanceled = false

        if (cleared) {
            lastSnapshot = 0u
            inFlight.clear()
            matches.clear()
        }

        if (pattern.isEmpty()) {
            resetMatches()
            processNewItemsTrivial()
            val wasCanceledSort = sortMatches()
            if (wasCanceledSort) {
                wasCanceled = true
            } else if (shouldNotify) {
                notify()
            }
            return
        }

        if (patternStatus == PatternStatus.Rescore) {
            resetMatches()
        }

        val unmatched = intArrayOf(0)
        if (patternStatus != PatternStatus.Unchanged && matches.isNotEmpty()) {
            processNewItemsTrivial()
            val currentMatcher = matchers.get()
            for (m in matches) {
                if (canceled) break
                if (m.idx == UInt.MAX_VALUE) {
                    unmatched[0]++
                    continue
                }
                val item = items.getUnchecked(m.idx)
                val score = pattern.score(item.matcherColumns, currentMatcher)
                if (score != null) {
                    m.score = score
                } else {
                    unmatched[0]++
                    m.score = 0L
                    m.idx = UInt.MAX_VALUE
                }
            }
        } else {
            processNewItems(unmatched)
        }

        val wasCanceledSort = sortMatches()
        if (wasCanceledSort) {
            wasCanceled = true
        } else {
            val removeCount = unmatched[0].coerceAtMost(matches.size)
            if (removeCount > 0) {
                val newSize = matches.size - removeCount
                while (matches.size > newSize) {
                    matches.removeAt(matches.size - 1)
                }
            }
            if (shouldNotify) {
                notify()
            }
        }
    }

    fun sortMatches(): Boolean {
        if (canceled) return true

        if (sortResults) {
            matches.sortWith { match1, match2 ->
                if (match1.score != match2.score) {
                    match2.score.compareTo(match1.score) // descending
                } else if (match1.idx == UInt.MAX_VALUE) {
                    1
                } else if (match2.idx == UInt.MAX_VALUE) {
                    -1
                } else {
                    val item1 = items.getUnchecked(match1.idx)
                    val item2 = items.getUnchecked(match2.idx)
                    val len1 = item1.matcherColumns.sumOf { it.length }
                    val len2 = item2.matcherColumns.sumOf { it.length }
                    if (len1 == len2) {
                        if (reverseItems) {
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
            matches.sortWith { match1, match2 ->
                if (match1.idx == UInt.MAX_VALUE) {
                    1
                } else if (match2.idx == UInt.MAX_VALUE) {
                    -1
                } else if (reverseItems) {
                    match2.idx.compareTo(match1.idx)
                } else {
                    match1.idx.compareTo(match2.idx)
                }
            }
        }
        return false
    }

    companion object {
        fun <T> new(
            workerThreads: Int?,
            config: Config,
            notify: () -> Unit,
            cols: UInt,
        ): Pair<Unit, Worker<T>> {
            val worker = Worker<T>(config, notify, cols)
            return Pair(Unit, worker)
        }
    }
}
