// port-lint: tests matcher/src/tests.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.chars.normalize
import io.github.kotlinmania.nucleo.pattern.CaseMatching
import io.github.kotlinmania.nucleo.pattern.Normalization
import io.github.kotlinmania.nucleo.pattern.Pattern
import kotlin.test.Test
import kotlin.test.assertEquals

enum class Algorithm {
    FuzzyOptimal,
    FuzzyGreedy,
    Substring,
    Prefix,
    Postfix,
    Exact,
}

data class MatchCase(
    val haystack: String,
    val needle: String,
    val indices: List<Int>,
    val score: Int,
)

class MatcherTest {
    private fun assertMatches(
        algorithms: List<Algorithm>,
        normalize: Boolean,
        caseSensitive: Boolean,
        path: Boolean,
        preferPrefix: Boolean,
        cases: List<MatchCase>,
    ) {
        val config =
            Config(
                normalize = normalize,
                ignoreCase = !caseSensitive,
                preferPrefix = preferPrefix,
            )
        if (path) {
            config.setMatchPaths()
        }
        val matcher = Matcher(config)
        val matchedIndices = mutableListOf<Int>()
        val needleBuf = mutableListOf<Char>()
        val haystackBuf = mutableListOf<Char>()

        for (case in cases) {
            val needleStr = if (!caseSensitive) case.needle.lowercase() else case.needle
            val needle = Utf32Str.new(needleStr, needleBuf)
            val haystack = Utf32Str.new(case.haystack, haystackBuf)
            val score = case.score + needle.len() * SCORE_MATCH

            for (algo in algorithms) {
                matchedIndices.clear()
                val res =
                    when (algo) {
                        Algorithm.FuzzyOptimal -> matcher.fuzzyIndices(haystack, needle, matchedIndices)
                        Algorithm.FuzzyGreedy -> matcher.fuzzyIndicesGreedy(haystack, needle, matchedIndices)
                        Algorithm.Substring -> matcher.substringIndices(haystack, needle, matchedIndices)
                        Algorithm.Prefix -> matcher.prefixIndices(haystack, needle, matchedIndices)
                        Algorithm.Postfix -> matcher.postfixIndices(haystack, needle, matchedIndices)
                        Algorithm.Exact -> matcher.exactIndices(haystack, needle, matchedIndices)
                    }

                val matchChars = matchedIndices.map { i -> normalize(haystack.get(i), matcher.config) }
                val needleChars = needle.chars().toList()

                assertEquals(
                    score,
                    res,
                    "$needle did not match $haystack: matched $matchChars $matchedIndices $algo",
                )
                assertEquals(
                    case.indices,
                    matchedIndices,
                    "$needle match $haystack $algo",
                )
                assertEquals(
                    needleChars,
                    matchChars,
                    "$needle match $haystack indices are incorrect $matchedIndices $algo",
                )
            }
        }
    }

    private fun assertNotMatchesWith(
        normalize: Boolean,
        caseSensitive: Boolean,
        algorithms: List<Algorithm>,
        cases: List<Pair<String, String>>,
    ) {
        val config =
            Config(
                normalize = normalize,
                ignoreCase = !caseSensitive,
            )
        val matcher = Matcher(config)
        val needleBuf = mutableListOf<Char>()
        val haystackBuf = mutableListOf<Char>()

        for ((haystackStr, needleStr) in cases) {
            val nStr = if (!caseSensitive) needleStr.lowercase() else needleStr
            val needle = Utf32Str.new(nStr, needleBuf)
            val haystack = Utf32Str.new(haystackStr, haystackBuf)

            for (algo in algorithms) {
                val res =
                    when (algo) {
                        Algorithm.FuzzyOptimal -> matcher.fuzzyMatch(haystack, needle)
                        Algorithm.FuzzyGreedy -> matcher.fuzzyMatchGreedy(haystack, needle)
                        Algorithm.Substring -> matcher.substringMatch(haystack, needle)
                        Algorithm.Prefix -> matcher.prefixMatch(haystack, needle)
                        Algorithm.Postfix -> matcher.postfixMatch(haystack, needle)
                        Algorithm.Exact -> matcher.exactMatch(haystack, needle)
                    }
                assertEquals(null, res, "$needle should not match $haystack $algo")
            }
        }
    }

    private fun assertNotMatches(normalize: Boolean, caseSensitive: Boolean, cases: List<Pair<String, String>>) {
        assertNotMatchesWith(
            normalize,
            caseSensitive,
            listOf(
                Algorithm.FuzzyOptimal,
                Algorithm.FuzzyGreedy,
                Algorithm.Substring,
                Algorithm.Prefix,
                Algorithm.Postfix,
                Algorithm.Exact,
            ),
            cases,
        )
    }

    private val bonusBoundaryWhite = Config.DEFAULT.bonusBoundaryWhite
    private val bonusBoundaryDelimiter = Config.DEFAULT.bonusBoundaryDelimiter

    @Test
    fun testFuzzy() {
        assertMatches(
            listOf(Algorithm.FuzzyGreedy, Algorithm.FuzzyOptimal),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "fooBarbaz1",
                    "obr",
                    listOf(2, 3, 5),
                    BONUS_CAMEL123 - PENALTY_GAP_START,
                ),
                MatchCase(
                    "/usr/share/doc/at/ChangeLog",
                    "changelog",
                    listOf(18, 19, 20, 21, 22, 23, 24, 25, 26),
                    (BONUS_FIRST_CHAR_MULTIPLIER + 8) * bonusBoundaryDelimiter,
                ),
                MatchCase(
                    "fooBarbaz1",
                    "br",
                    listOf(3, 5),
                    BONUS_CAMEL123 * BONUS_FIRST_CHAR_MULTIPLIER - PENALTY_GAP_START,
                ),
                MatchCase(
                    "foo bar baz",
                    "fbb",
                    listOf(0, 4, 8),
                    bonusBoundaryWhite * BONUS_FIRST_CHAR_MULTIPLIER + bonusBoundaryWhite * 2 - 2 * PENALTY_GAP_START - 4 * PENALTY_GAP_EXTENSION,
                ),
                MatchCase(
                    "/AutomatorDocument.icns",
                    "rdoc",
                    listOf(9, 10, 11, 12),
                    BONUS_CAMEL123 + 2 * BONUS_CONSECUTIVE,
                ),
                MatchCase(
                    "/man1/zshcompctl.1",
                    "zshc",
                    listOf(6, 7, 8, 9),
                    bonusBoundaryDelimiter * (BONUS_FIRST_CHAR_MULTIPLIER + 3),
                ),
                MatchCase(
                    "/.oh-my-zsh/cache",
                    "zshc",
                    listOf(8, 9, 10, 12),
                    BONUS_BOUNDARY * (BONUS_FIRST_CHAR_MULTIPLIER + 2) - PENALTY_GAP_START + bonusBoundaryDelimiter,
                ),
                MatchCase(
                    "ab0123 456",
                    "12356",
                    listOf(3, 4, 5, 8, 9),
                    BONUS_CONSECUTIVE * 3 - PENALTY_GAP_START - PENALTY_GAP_EXTENSION,
                ),
                MatchCase(
                    "abc123 456",
                    "12356",
                    listOf(3, 4, 5, 8, 9),
                    BONUS_CAMEL123 * (BONUS_FIRST_CHAR_MULTIPLIER + 2) - PENALTY_GAP_START - PENALTY_GAP_EXTENSION + BONUS_CONSECUTIVE,
                ),
                MatchCase(
                    "foo/bar/baz",
                    "fbb",
                    listOf(0, 4, 8),
                    bonusBoundaryWhite * BONUS_FIRST_CHAR_MULTIPLIER + bonusBoundaryDelimiter * 2 - 2 * PENALTY_GAP_START - 4 * PENALTY_GAP_EXTENSION,
                ),
                MatchCase(
                    "fooBarBaz",
                    "fbb",
                    listOf(0, 3, 6),
                    bonusBoundaryWhite * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_CAMEL123 * 2 - 2 * PENALTY_GAP_START - 2 * PENALTY_GAP_EXTENSION,
                ),
                MatchCase(
                    "foo barbaz",
                    "fbb",
                    listOf(0, 4, 7),
                    bonusBoundaryWhite * BONUS_FIRST_CHAR_MULTIPLIER + bonusBoundaryWhite - PENALTY_GAP_START * 2 - PENALTY_GAP_EXTENSION * 3,
                ),
                MatchCase(
                    "fooBar Baz",
                    "foob",
                    listOf(0, 1, 2, 3),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 3),
                ),
                MatchCase(
                    "xFoo-Bar Baz",
                    "foo-b",
                    listOf(1, 2, 3, 4, 5),
                    BONUS_CAMEL123 * (BONUS_FIRST_CHAR_MULTIPLIER + 3) + BONUS_BOUNDARY,
                ),
            ),
        )
    }

    @Test
    fun emptyNeedle() {
        assertMatches(
            listOf(
                Algorithm.Substring,
                Algorithm.Prefix,
                Algorithm.Postfix,
                Algorithm.FuzzyGreedy,
                Algorithm.FuzzyOptimal,
                Algorithm.Exact,
            ),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(MatchCase("foo bar baz", "", listOf(), 0)),
        )
    }

    @Test
    fun testSubstring() {
        assertMatches(
            listOf(Algorithm.Substring, Algorithm.Prefix),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "foo bar baz",
                    "foo",
                    listOf(0, 1, 2),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2),
                ),
                MatchCase(
                    " foo bar baz",
                    "FOO",
                    listOf(1, 2, 3),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2),
                ),
                MatchCase(
                    " foo bar baz",
                    " FOO",
                    listOf(0, 1, 2, 3),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 3),
                ),
            ),
        )

        assertMatches(
            listOf(Algorithm.Substring, Algorithm.Postfix),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "foo bar baz",
                    "baz",
                    listOf(8, 9, 10),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2),
                ),
                MatchCase(
                    "foo bar baz ",
                    "baz",
                    listOf(8, 9, 10),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2),
                ),
                MatchCase(
                    "foo bar baz ",
                    "baz ",
                    listOf(8, 9, 10, 11),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 3),
                ),
            ),
        )

        assertMatches(
            listOf(
                Algorithm.Substring,
                Algorithm.Prefix,
                Algorithm.Postfix,
                Algorithm.Exact,
                Algorithm.FuzzyGreedy,
                Algorithm.FuzzyOptimal,
            ),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "foo",
                    "foo",
                    listOf(0, 1, 2),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2),
                ),
                MatchCase(
                    " foo",
                    "foo",
                    listOf(1, 2, 3),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2),
                ),
                MatchCase(
                    " foo",
                    " foo",
                    listOf(0, 1, 2, 3),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 3),
                ),
            ),
        )

        assertMatches(
            listOf(Algorithm.Substring),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "fooBarbaz1",
                    "oba",
                    listOf(2, 3, 4),
                    BONUS_CAMEL123 + BONUS_CONSECUTIVE,
                ),
                MatchCase(
                    "/AutomatorDocument.icns",
                    "rdoc",
                    listOf(9, 10, 11, 12),
                    BONUS_CAMEL123 + 2 * BONUS_CONSECUTIVE,
                ),
                MatchCase(
                    "/man1/zshcompctl.1",
                    "zshc",
                    listOf(6, 7, 8, 9),
                    bonusBoundaryDelimiter * (BONUS_FIRST_CHAR_MULTIPLIER + 3),
                ),
                MatchCase(
                    "/.oh-my-zsh/cache",
                    "zsh/c",
                    listOf(8, 9, 10, 11, 12),
                    BONUS_BOUNDARY * (BONUS_FIRST_CHAR_MULTIPLIER + 3) + bonusBoundaryDelimiter,
                ),
            ),
        )

        assertNotMatchesWith(
            normalize = true,
            caseSensitive = false,
            algorithms = listOf(Algorithm.Prefix, Algorithm.Substring, Algorithm.Postfix, Algorithm.Exact),
            cases =
                listOf(
                    Pair(
                        "At the Road\u2019s End - Seeming - SOL: A Self-Banishment Ritual",
                        "adi",
                    ),
                ),
        )
    }

    @Test
    fun testSubstringCaseSensitive() {
        assertMatches(
            listOf(Algorithm.Substring, Algorithm.Prefix),
            normalize = false,
            caseSensitive = true,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "Foo bar baz",
                    "Foo",
                    listOf(0, 1, 2),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2),
                ),
                MatchCase(
                    "F\u022B\u00F4 bar baz",
                    "F\u022B\u00F4",
                    listOf(0, 1, 2),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2),
                ),
                MatchCase(
                    "Foo \u0E3Far baz",
                    "Foo",
                    listOf(0, 1, 2),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2),
                ),
            ),
        )
        assertNotMatchesWith(
            normalize = false,
            caseSensitive = true,
            algorithms = listOf(Algorithm.Substring, Algorithm.Prefix),
            cases = listOf(Pair("foo bar baz", "Foo")),
        )
    }

    @Test
    fun testFuzzyCaseSensitive() {
        assertMatches(
            listOf(Algorithm.FuzzyGreedy, Algorithm.FuzzyOptimal),
            normalize = false,
            caseSensitive = true,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "fooBarbaz1",
                    "oBr",
                    listOf(2, 3, 5),
                    BONUS_CAMEL123 - PENALTY_GAP_START,
                ),
                MatchCase(
                    "Foo/Bar/Baz",
                    "FBB",
                    listOf(0, 4, 8),
                    bonusBoundaryWhite * BONUS_FIRST_CHAR_MULTIPLIER + bonusBoundaryDelimiter * 2 - 2 * PENALTY_GAP_START - 4 * PENALTY_GAP_EXTENSION,
                ),
                MatchCase(
                    "FooBarBaz",
                    "FBB",
                    listOf(0, 3, 6),
                    bonusBoundaryWhite * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_CAMEL123 * 2 - 2 * PENALTY_GAP_START - 2 * PENALTY_GAP_EXTENSION,
                ),
                MatchCase(
                    "FooBar Baz",
                    "FooB",
                    listOf(0, 1, 2, 3),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 3),
                ),
                MatchCase(
                    "foo-bar",
                    "o-ba",
                    listOf(2, 3, 4, 5),
                    BONUS_NON_WORD + BONUS_BOUNDARY * 2,
                ),
            ),
        )
    }

    @Test
    fun testNormalize() {
        assertMatches(
            listOf(Algorithm.FuzzyGreedy, Algorithm.FuzzyOptimal),
            normalize = true,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "S\u00F3 Dan\u00E7o Samba",
                    "So",
                    listOf(0, 1),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 1),
                ),
                MatchCase(
                    "S\u00F3 Dan\u00E7o Samba",
                    "sodc",
                    listOf(0, 1, 3, 6),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 1) - PENALTY_GAP_START + bonusBoundaryWhite - PENALTY_GAP_START - PENALTY_GAP_EXTENSION,
                ),
                MatchCase(
                    "Dan\u00E7o",
                    "danco",
                    listOf(0, 1, 2, 3, 4),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 4),
                ),
                MatchCase(
                    "Dan\u00C7o",
                    "danco",
                    listOf(0, 1, 2, 3, 4),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 4),
                ),
                MatchCase(
                    "x\u00C7ando",
                    "cando",
                    listOf(1, 2, 3, 4, 5),
                    BONUS_CAMEL123 * (BONUS_FIRST_CHAR_MULTIPLIER + 4),
                ),
                MatchCase(
                    "\u0621(GCG\u0274CG",
                    "n",
                    listOf(5),
                    0,
                ),
            ),
        )
    }

    @Test
    fun testUnicode() {
        assertMatches(
            listOf(Algorithm.FuzzyGreedy, Algorithm.FuzzyOptimal, Algorithm.Substring),
            normalize = true,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "你好世界",
                    "你好",
                    listOf(0, 1),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 1),
                ),
                MatchCase(
                    " 你好世界",
                    "你好",
                    listOf(1, 2),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 1),
                ),
            ),
        )
        assertMatches(
            listOf(Algorithm.FuzzyGreedy, Algorithm.FuzzyOptimal),
            normalize = true,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "你好世界",
                    "你世",
                    listOf(0, 2),
                    bonusBoundaryWhite * BONUS_FIRST_CHAR_MULTIPLIER - PENALTY_GAP_START,
                ),
            ),
        )
        assertNotMatches(
            normalize = false,
            caseSensitive = false,
            listOf(Pair("Flibbertigibbet / イタズラっ子たち", "lying")),
        )
    }

    @Test
    fun testLongStr() {
        assertMatches(
            listOf(Algorithm.FuzzyGreedy, Algorithm.FuzzyOptimal),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "x".repeat(65536),
                    "xx",
                    listOf(0, 1),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 1),
                ),
            ),
        )
    }

    @Test
    fun testCasing() {
        assertMatches(
            listOf(Algorithm.FuzzyGreedy, Algorithm.FuzzyOptimal),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "fooBar",
                    "foobar",
                    listOf(0, 1, 2, 3, 4, 5),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 5),
                ),
                MatchCase(
                    "foobar",
                    "foobar",
                    listOf(0, 1, 2, 3, 4, 5),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 5),
                ),
                MatchCase(
                    "foo-bar",
                    "foobar",
                    listOf(0, 1, 2, 4, 5, 6),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2) - PENALTY_GAP_START + BONUS_BOUNDARY * 3,
                ),
                MatchCase(
                    "foo_bar",
                    "foobar",
                    listOf(0, 1, 2, 4, 5, 6),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2) - PENALTY_GAP_START + BONUS_BOUNDARY * 3,
                ),
            ),
        )
    }

    @Test
    fun testOptimal() {
        assertMatches(
            listOf(Algorithm.FuzzyOptimal),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "axxx xx ",
                    "xx",
                    listOf(5, 6),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 1),
                ),
                MatchCase(
                    "SS!H",
                    "S!",
                    listOf(0, 2),
                    bonusBoundaryWhite * BONUS_FIRST_CHAR_MULTIPLIER - PENALTY_GAP_START + BONUS_NON_WORD,
                ),
                MatchCase(
                    "xf.foo",
                    "xfoo",
                    listOf(0, 3, 4, 5),
                    bonusBoundaryWhite * BONUS_FIRST_CHAR_MULTIPLIER - PENALTY_GAP_START - PENALTY_GAP_EXTENSION + BONUS_BOUNDARY * 3,
                ),
                MatchCase(
                    "xf fo",
                    "xfo",
                    listOf(0, 3, 4),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 2) - PENALTY_GAP_START - PENALTY_GAP_EXTENSION,
                ),
            ),
        )
    }

    @Test
    fun testReject() {
        assertNotMatches(
            normalize = true,
            caseSensitive = false,
            listOf(
                Pair("你好界", "abc"),
                Pair("你好界", "a"),
                Pair("你好世界", "富"),
                Pair("Só Danço Samba", "sox"),
                Pair("fooBarbaz", "fooBarbazz"),
                Pair("fooBarbaz", "c"),
            ),
        )
        assertNotMatches(
            normalize = true,
            caseSensitive = true,
            listOf(
                Pair("你好界", "abc"),
                Pair("abc", "你"),
                Pair("abc", "A"),
                Pair("abc", "d"),
                Pair("你好世界", "富"),
                Pair("Só Danço Samba", "sox"),
                Pair("fooBarbaz", "oBZ"),
                Pair("Foo Bar Baz", "fbb"),
                Pair("fooBarbaz", "fooBarbazz"),
            ),
        )
        assertNotMatches(
            normalize = false,
            caseSensitive = true,
            listOf(
                Pair("Só Danço Samba", "sod"),
                Pair("Só Danço Samba", "soc"),
                Pair("Só Danç", "So"),
            ),
        )
        assertNotMatches(
            normalize = false,
            caseSensitive = false,
            listOf(Pair("ۂۂfoۂۂ", "foo")),
        )
    }

    @Test
    fun testPreferPrefix() {
        assertMatches(
            listOf(Algorithm.FuzzyOptimal, Algorithm.FuzzyGreedy),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = true,
            listOf(
                MatchCase(
                    "Moby Dick",
                    "md",
                    listOf(0, 5),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 1) + MAX_PREFIX_BONUS - PENALTY_GAP_START - 3 * PENALTY_GAP_EXTENSION,
                ),
                MatchCase(
                    "Though I cannot tell why it was exactly that those stage managers, the Fates, put me down for this shabby part of a whaling voyage",
                    "md",
                    listOf(82, 85),
                    bonusBoundaryWhite * (BONUS_FIRST_CHAR_MULTIPLIER + 1) - PENALTY_GAP_START - PENALTY_GAP_EXTENSION,
                ),
            ),
        )
    }

    @Test
    fun testSingleCharNeedle() {
        assertMatches(
            listOf(Algorithm.FuzzyOptimal),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "foO",
                    "o",
                    listOf(2),
                    BONUS_FIRST_CHAR_MULTIPLIER * BONUS_CAMEL123,
                ),
            ),
        )
        assertMatches(
            listOf(Algorithm.FuzzyOptimal),
            normalize = false,
            caseSensitive = false,
            path = false,
            preferPrefix = false,
            listOf(
                MatchCase(
                    "f\u00F6\u00D6",
                    "\u00F6",
                    listOf(2),
                    BONUS_FIRST_CHAR_MULTIPLIER * BONUS_CAMEL123,
                ),
            ),
        )
    }

    @Test
    fun umlaut() {
        val paths = listOf("be", "b\u00EB")
        val matcher = Matcher(Config.DEFAULT)
        val matches1 =
            Pattern
                .parse("\u00EB", CaseMatching.Ignore, Normalization.Smart)
                .matchList(paths, matcher)
        assertEquals(1, matches1.size)

        val matches2 =
            Pattern
                .parse("e", CaseMatching.Ignore, Normalization.Never)
                .matchList(paths, matcher)
        assertEquals(1, matches2.size)

        val matches3 =
            Pattern
                .parse("e", CaseMatching.Ignore, Normalization.Smart)
                .matchList(paths, matcher)
        assertEquals(2, matches3.size)
    }
}
