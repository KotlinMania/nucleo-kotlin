// port-lint: source matcher/src/pattern.rs
package io.github.kotlinmania.nucleo.pattern

import io.github.kotlinmania.nucleo.Matcher
import io.github.kotlinmania.nucleo.Utf32Str
import io.github.kotlinmania.nucleo.Utf32String
import io.github.kotlinmania.nucleo.chars.graphemes
import io.github.kotlinmania.nucleo.chars.isUpperCase
import io.github.kotlinmania.nucleo.chars.normalize
import io.github.kotlinmania.nucleo.chars.toLowerCase

/**
 * How to treat a case mismatch between two characters.
 */
public enum class CaseMatching {
    /**
     * Characters never match their case folded version (`a != A`).
     */
    Respect,

    /**
     * Characters always match their case folded version (`a == A`).
     */
    Ignore,

    /**
     * Acts like [Ignore] if all characters in a pattern atom are lowercase and like [Respect] otherwise.
     */
    Smart,
}

/**
 * How to handle Unicode normalization.
 */
public enum class Normalization {
    /**
     * Characters never match their normalized version (`a != ä`).
     */
    Never,

    /**
     * Acts like [Never] if any character in a pattern atom would need to be normalized.
     * Otherwise normalization occurs (`a == ä` but `ä != a`).
     */
    Smart,
}

/**
 * The kind of matching algorithm to run for an atom.
 */
public enum class AtomKind {
    /**
     * Fuzzy matching where the needle must match any haystack characters (can contain gaps).
     */
    Fuzzy,

    /**
     * The needle must match a contiguous sequence of haystack characters without gaps.
     */
    Substring,

    /**
     * The needle must match all leading haystack characters without gaps or prefix.
     */
    Prefix,

    /**
     * The needle must match all trailing haystack characters without gaps or postfix.
     */
    Postfix,

    /**
     * The needle must match all haystack characters without gaps or prefix.
     */
    Exact,
}

/**
 * A single pattern component that is matched with a single [Matcher] function.
 */
public data class Atom(
    public var negative: Boolean,
    public var kind: AtomKind,
    public val needle: Utf32String,
    public val ignoreCase: Boolean,
    public val normalize: Boolean,
) {
    /**
     * Matches this pattern against `haystack` and calculates a ranking score.
     */
    public fun score(haystack: Utf32Str, matcher: Matcher): Int? {
        matcher.config.ignoreCase = ignoreCase
        matcher.config.normalize = normalize
        val patternScore =
            when (kind) {
                AtomKind.Exact -> matcher.exactMatch(haystack, needle.slice())
                AtomKind.Fuzzy -> matcher.fuzzyMatch(haystack, needle.slice())
                AtomKind.Substring -> matcher.substringMatch(haystack, needle.slice())
                AtomKind.Prefix -> matcher.prefixMatch(haystack, needle.slice())
                AtomKind.Postfix -> matcher.postfixMatch(haystack, needle.slice())
            }
        return if (negative) {
            if (patternScore != null) null else 0
        } else {
            patternScore
        }
    }

    /**
     * Matches this pattern against `haystack`, calculates a ranking score and appends match indices.
     */
    public fun indices(haystack: Utf32Str, matcher: Matcher, indices: MutableList<Int>): Int? {
        matcher.config.ignoreCase = ignoreCase
        matcher.config.normalize = normalize
        return if (negative) {
            val patternScore =
                when (kind) {
                    AtomKind.Exact -> matcher.exactMatch(haystack, needle.slice())
                    AtomKind.Fuzzy -> matcher.fuzzyMatch(haystack, needle.slice())
                    AtomKind.Substring -> matcher.substringMatch(haystack, needle.slice())
                    AtomKind.Prefix -> matcher.prefixMatch(haystack, needle.slice())
                    AtomKind.Postfix -> matcher.postfixMatch(haystack, needle.slice())
                }
            if (patternScore == null) 0 else null
        } else {
            when (kind) {
                AtomKind.Exact -> matcher.exactIndices(haystack, needle.slice(), indices)
                AtomKind.Fuzzy -> matcher.fuzzyIndices(haystack, needle.slice(), indices)
                AtomKind.Substring -> matcher.substringIndices(haystack, needle.slice(), indices)
                AtomKind.Prefix -> matcher.prefixIndices(haystack, needle.slice(), indices)
                AtomKind.Postfix -> matcher.postfixIndices(haystack, needle.slice(), indices)
            }
        }
    }

    /**
     * Returns the needle text as a [Utf32Str].
     */
    public fun needleText(): Utf32Str = needle.slice()

    /**
     * Convenience function to match and sort a list of inputs.
     */
    public fun <T> matchList(items: Iterable<T>, matcher: Matcher, transform: (T) -> String = { it.toString() }): List<Pair<T, Int>> {
        if (needle.isEmpty()) {
            return items.map { Pair(it, 0) }
        }
        val buf = mutableListOf<Char>()
        val matches = mutableListOf<Pair<T, Int>>()
        for (item in items) {
            val text = transform(item)
            val s = score(Utf32Str.new(text, buf), matcher)
            if (s != null) {
                matches.add(Pair(item, s))
            }
        }
        matches.sortByDescending { it.second }
        return matches
    }

    public companion object {
        public fun new(
            needle: String,
            case: CaseMatching,
            normalize: Normalization,
            kind: AtomKind,
            escapeWhitespace: Boolean,
        ): Atom = newInner(needle, case, normalize, kind, escapeWhitespace, false)

        private fun newInner(
            needleStr: String,
            case: CaseMatching,
            normalization: Normalization,
            kind: AtomKind,
            escapeWhitespace: Boolean,
            appendDollar: Boolean,
        ): Atom {
            var ignoreCase: Boolean
            var norm = normalization == Normalization.Smart

            val isAscii = needleStr.all { it.code <= 127 }
            val needle =
                if (isAscii) {
                    var processed =
                        if (escapeWhitespace && needleStr.contains("\\ ")) {
                            needleStr.replace("\\ ", " ")
                        } else {
                            needleStr
                        }
                    when (case) {
                        CaseMatching.Ignore -> {
                            ignoreCase = true
                            processed = processed.lowercase()
                        }
                        CaseMatching.Smart -> {
                            ignoreCase = !processed.any { it in 'A'..'Z' }
                        }
                        CaseMatching.Respect -> {
                            ignoreCase = false
                        }
                    }
                    if (appendDollar) {
                        processed += "$"
                    }
                    Utf32String.Ascii(processed)
                } else {
                    val list = mutableListOf<Char>()
                    ignoreCase = case == CaseMatching.Ignore || case == CaseMatching.Smart
                    norm = normalization == Normalization.Smart

                    if (escapeWhitespace) {
                        var sawBackslash = false
                        for (c in graphemes(needleStr)) {
                            var ch = c
                            if (sawBackslash) {
                                if (ch == ' ') {
                                    list.add(' ')
                                    sawBackslash = false
                                    continue
                                } else {
                                    list.add('\\')
                                }
                            }
                            sawBackslash = (ch == '\\')
                            if (!sawBackslash) {
                                when (case) {
                                    CaseMatching.Ignore -> ch = toLowerCase(ch)
                                    CaseMatching.Smart -> ignoreCase = ignoreCase && !isUpperCase(ch)
                                    CaseMatching.Respect -> {}
                                }
                                if (normalization == Normalization.Smart) {
                                    norm = norm && (normalize(ch) == ch)
                                }
                                list.add(ch)
                            }
                        }
                    } else {
                        for (c in graphemes(needleStr)) {
                            var ch = c
                            when (case) {
                                CaseMatching.Ignore -> ch = toLowerCase(ch)
                                CaseMatching.Smart -> ignoreCase = ignoreCase && !isUpperCase(ch)
                                CaseMatching.Respect -> {}
                            }
                            if (normalization == Normalization.Smart) {
                                norm = norm && (normalize(ch) == ch)
                            }
                            list.add(ch)
                        }
                    }
                    if (appendDollar) {
                        list.add('$')
                    }
                    Utf32String.Unicode(list.toCharArray())
                }

            return Atom(
                negative = false,
                kind = kind,
                needle = needle,
                ignoreCase = ignoreCase,
                normalize = norm,
            )
        }

        public fun parse(raw: String, case: CaseMatching, normalize: Normalization): Atom {
            var atom = raw
            val invert: Boolean
            if (atom.startsWith("!")) {
                atom = atom.substring(1)
                invert = true
            } else if (atom.startsWith("\\!")) {
                atom = atom.substring(1)
                invert = false
            } else {
                invert = false
            }

            var kind: AtomKind =
                when {
                    atom.startsWith("^") -> {
                        atom = atom.substring(1)
                        AtomKind.Prefix
                    }
                    atom.startsWith("'") -> {
                        atom = atom.substring(1)
                        AtomKind.Substring
                    }
                    atom.startsWith("\\^") || atom.startsWith("\\'") -> {
                        atom = atom.substring(1)
                        AtomKind.Fuzzy
                    }
                    else -> AtomKind.Fuzzy
                }

            var appendDollar = false
            if (atom.endsWith("\\$")) {
                appendDollar = true
                atom = atom.substring(0, atom.length - 2)
            } else if (atom.endsWith("$")) {
                kind = if (kind == AtomKind.Fuzzy) AtomKind.Postfix else AtomKind.Exact
                atom = atom.substring(0, atom.length - 1)
            }

            if (invert && kind == AtomKind.Fuzzy) {
                kind = AtomKind.Substring
            }

            val pattern = newInner(atom, case, normalize, kind, escapeWhitespace = true, appendDollar = appendDollar)
            pattern.negative = invert
            return pattern
        }
    }
}

internal fun patternAtoms(pattern: String): Sequence<String> =
    sequence {
        var sawBackslash = false
        var current = StringBuilder()
        for (i in 0 until pattern.length) {
            val c = pattern[i]
            if (c.isWhitespace() && !sawBackslash) {
                if (current.isNotEmpty()) {
                    yield(current.toString())
                    current = StringBuilder()
                }
            } else {
                current.append(c)
                sawBackslash = (c == '\\' && !sawBackslash)
            }
        }
        if (current.isNotEmpty()) {
            yield(current.toString())
        }
    }

/**
 * A text pattern made up of multiple [Atom]s.
 */
public class Pattern(
    atoms: List<Atom> = emptyList(),
) {
    private val _atoms: MutableList<Atom> = atoms.toMutableList()

    /**
     * The individual pattern atoms in this pattern.
     */
    public val atoms: List<Atom> get() = _atoms

    /**
     * Creates a deep copy of this pattern.
     */
    public fun clone(): Pattern = Pattern(_atoms.map { it.copy() })

    /**
     * Clones data from another pattern into this one.
     */
    public fun cloneFrom(other: Pattern) {
        _atoms.clear()
        _atoms.addAll(other._atoms.map { it.copy() })
    }

    /**
     * Matches this pattern against `haystack` and calculates a ranking score.
     */
    public fun score(haystack: Utf32Str, matcher: Matcher): Long? {
        if (_atoms.isEmpty()) return 0L
        var total = 0L
        for (atom in _atoms) {
            val s = atom.score(haystack, matcher) ?: return null
            total += s
        }
        return total
    }

    /**
     * Matches this pattern against `haystack`, calculates a ranking score and appends match indices.
     */
    public fun indices(haystack: Utf32Str, matcher: Matcher, indices: MutableList<Int>): Long? {
        if (_atoms.isEmpty()) return 0L
        var total = 0L
        for (atom in _atoms) {
            val s = atom.indices(haystack, matcher, indices) ?: return null
            total += s
        }
        return total
    }

    /**
     * Refreshes this pattern by reparsing it from a string.
     */
    public fun reparse(pattern: String, caseMatching: CaseMatching, normalize: Normalization) {
        _atoms.clear()
        for (pat in patternAtoms(pattern)) {
            val atom = Atom.parse(pat, caseMatching, normalize)
            if (!atom.needle.isEmpty()) {
                _atoms.add(atom)
            }
        }
    }

    override fun equals(other: Any?): Boolean =
        other is Pattern && _atoms == other._atoms

    override fun hashCode(): Int = _atoms.hashCode()

    /**
     * Convenience function to match and sort a list of inputs.
     */
    public fun <T> matchList(items: Iterable<T>, matcher: Matcher, transform: (T) -> String = { it.toString() }): List<Pair<T, Long>> {
        if (atoms.isEmpty()) {
            return items.map { Pair(it, 0L) }
        }
        val buf = mutableListOf<Char>()
        val matches = mutableListOf<Pair<T, Long>>()
        for (item in items) {
            val text = transform(item)
            val s = score(Utf32Str.new(text, buf), matcher)
            if (s != null) {
                matches.add(Pair(item, s))
            }
        }
        matches.sortByDescending { it.second }
        return matches
    }

    public companion object {
        public fun new(
            pattern: String,
            caseMatching: CaseMatching,
            normalize: Normalization,
            kind: AtomKind,
        ): Pattern {
            val atoms = mutableListOf<Atom>()
            for (pat in patternAtoms(pattern)) {
                val atom = Atom.new(pat, caseMatching, normalize, kind, true)
                if (!atom.needle.isEmpty()) {
                    atoms.add(atom)
                }
            }
            return Pattern(atoms)
        }

        public fun parse(
            pattern: String,
            caseMatching: CaseMatching,
            normalize: Normalization,
        ): Pattern {
            val atoms = mutableListOf<Atom>()
            for (pat in patternAtoms(pattern)) {
                val atom = Atom.parse(pat, caseMatching, normalize)
                if (!atom.needle.isEmpty()) {
                    atoms.add(atom)
                }
            }
            return Pattern(atoms)
        }
    }
}
