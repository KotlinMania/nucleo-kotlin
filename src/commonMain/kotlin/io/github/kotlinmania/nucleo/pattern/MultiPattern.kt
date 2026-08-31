// port-lint: source src/pattern.rs
package io.github.kotlinmania.nucleo.pattern

import io.github.kotlinmania.nucleo.Matcher
import io.github.kotlinmania.nucleo.Utf32String

/**
 * Status of the multi-pattern match columns.
 */
public enum class Status {
    Unchanged,
    Update,
    Rescore,
}

/**
 * A multi-column pattern matcher.
 */
public class MultiPattern(
    columns: Int,
) {
    internal val cols: MutableList<Pair<Pattern, Status>> =
        MutableList(columns) { Pattern(mutableListOf()) to Status.Unchanged }

    /**
     * Creates a deep copy of this [MultiPattern].
     */
    public fun clone(): MultiPattern {
        val copy = MultiPattern(cols.size)
        for (i in cols.indices) {
            val (pattern, status) = cols[i]
            val clonedAtoms = pattern.atoms.map { it.copy() }.toMutableList()
            copy.cols[i] = Pattern(clonedAtoms) to status
        }
        return copy
    }

    /**
     * Clones data from [source] into this [MultiPattern].
     */
    public fun cloneFrom(source: MultiPattern) {
        cols.clear()
        for ((pattern, status) in source.cols) {
            val clonedAtoms = pattern.atoms.map { it.copy() }.toMutableList()
            cols.add(Pattern(clonedAtoms) to status)
        }
    }

    /**
     * Reparses a column. By specifying `append` the caller promises that text passed
     * to the previous `reparse` invocation is a prefix of `newText`.
     */
    public fun reparse(
        column: Int,
        newText: String,
        caseMatching: CaseMatching,
        normalization: Normalization,
        append: Boolean,
    ) {
        val oldStatus = cols[column].second
        val lastAtom = cols[column].first.atoms.lastOrNull()
        val newStatus =
            if (append && oldStatus != Status.Rescore && (lastAtom == null || !lastAtom.negative)) {
                Status.Update
            } else {
                Status.Rescore
            }
        val pat = cols[column].first
        pat.reparse(newText, caseMatching, normalization)
        cols[column] = pat to newStatus
    }

    /**
     * Returns the pattern for the specified column.
     */
    public fun columnPattern(column: Int): Pattern = cols[column].first

    /**
     * Returns the highest status across all columns.
     */
    public fun status(): Status =
        cols.map { it.second }.maxOrNull() ?: Status.Unchanged

    /**
     * Resets the status of all columns to [Status.Unchanged].
     */
    public fun resetStatus() {
        for (i in cols.indices) {
            cols[i] = cols[i].first to Status.Unchanged
        }
    }

    /**
     * Matches this pattern against a multi-column haystack and calculates a total ranking score.
     */
    public fun score(haystack: Array<Utf32String>, matcher: Matcher): Long? {
        var total = 0L
        for (i in 0 until minOf(cols.size, haystack.size)) {
            val s = cols[i].first.score(haystack[i].slice(), matcher) ?: return null
            total += s
        }
        return total
    }

    /**
     * Matches this pattern against a multi-column haystack and calculates a total ranking score.
     */
    public fun score(haystack: List<Utf32String>, matcher: Matcher): Long? {
        var total = 0L
        for (i in 0 until minOf(cols.size, haystack.size)) {
            val s = cols[i].first.score(haystack[i].slice(), matcher) ?: return null
            total += s
        }
        return total
    }

    /**
     * Checks whether all column patterns are empty.
     */
    public fun isEmpty(): Boolean = cols.all { it.first.atoms.isEmpty() }

    public companion object {
        /**
         * Creates a new [MultiPattern] for the given number of columns.
         */
        public fun new(columns: Int): MultiPattern = MultiPattern(columns)
    }
}
