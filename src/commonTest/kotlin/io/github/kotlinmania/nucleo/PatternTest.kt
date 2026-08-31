// port-lint: tests matcher/src/pattern/tests.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.pattern.Atom
import io.github.kotlinmania.nucleo.pattern.AtomKind
import io.github.kotlinmania.nucleo.pattern.CaseMatching
import io.github.kotlinmania.nucleo.pattern.Normalization
import io.github.kotlinmania.nucleo.pattern.Pattern
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatternTest {
    @Test
    fun negative() {
        val pat = Atom.parse("!foo", CaseMatching.Smart, Normalization.Smart)
        assertTrue(pat.negative)
        assertEquals(AtomKind.Substring, pat.kind)
        assertEquals("foo", pat.needle.toString())

        val pat2 = Atom.parse("!^foo", CaseMatching.Smart, Normalization.Smart)
        assertTrue(pat2.negative)
        assertEquals(AtomKind.Prefix, pat2.kind)
        assertEquals("foo", pat2.needle.toString())

        val pat3 = Atom.parse("!foo$", CaseMatching.Smart, Normalization.Smart)
        assertTrue(pat3.negative)
        assertEquals(AtomKind.Postfix, pat3.kind)
        assertEquals("foo", pat3.needle.toString())

        val pat4 = Atom.parse("!^foo$", CaseMatching.Smart, Normalization.Smart)
        assertTrue(pat4.negative)
        assertEquals(AtomKind.Exact, pat4.kind)
        assertEquals("foo", pat4.needle.toString())
    }

    @Test
    fun patternKinds() {
        val pat = Atom.parse("foo", CaseMatching.Smart, Normalization.Smart)
        assertFalse(pat.negative)
        assertEquals(AtomKind.Fuzzy, pat.kind)
        assertEquals("foo", pat.needle.toString())

        val pat2 = Atom.parse("'foo", CaseMatching.Smart, Normalization.Smart)
        assertFalse(pat2.negative)
        assertEquals(AtomKind.Substring, pat2.kind)
        assertEquals("foo", pat2.needle.toString())

        val pat3 = Atom.parse("^foo", CaseMatching.Smart, Normalization.Smart)
        assertFalse(pat3.negative)
        assertEquals(AtomKind.Prefix, pat3.kind)
        assertEquals("foo", pat3.needle.toString())

        val pat4 = Atom.parse("foo$", CaseMatching.Smart, Normalization.Smart)
        assertFalse(pat4.negative)
        assertEquals(AtomKind.Postfix, pat4.kind)
        assertEquals("foo", pat4.needle.toString())

        val pat5 = Atom.parse("^foo$", CaseMatching.Smart, Normalization.Smart)
        assertFalse(pat5.negative)
        assertEquals(AtomKind.Exact, pat5.kind)
        assertEquals("foo", pat5.needle.toString())
    }

    @Test
    fun caseMatching() {
        val pat = Atom.parse("foo", CaseMatching.Smart, Normalization.Smart)
        assertTrue(pat.ignoreCase)
        assertEquals("foo", pat.needle.toString())

        val pat2 = Atom.parse("Foo", CaseMatching.Smart, Normalization.Smart)
        assertFalse(pat2.ignoreCase)
        assertEquals("Foo", pat2.needle.toString())

        val pat3 = Atom.parse("Foo", CaseMatching.Ignore, Normalization.Smart)
        assertTrue(pat3.ignoreCase)
        assertEquals("foo", pat3.needle.toString())

        val pat4 = Atom.parse("Foo", CaseMatching.Respect, Normalization.Smart)
        assertFalse(pat4.ignoreCase)
        assertEquals("Foo", pat4.needle.toString())

        val pat5 = Atom.parse("Äxx", CaseMatching.Ignore, Normalization.Smart)
        assertTrue(pat5.ignoreCase)
        assertEquals("äxx", pat5.needle.toString())

        val pat6 = Atom.parse("Äxx", CaseMatching.Respect, Normalization.Smart)
        assertFalse(pat6.ignoreCase)

        val pat7 = Atom.parse("Axx", CaseMatching.Smart, Normalization.Smart)
        assertFalse(pat7.ignoreCase)
        assertEquals("Axx", pat7.needle.toString())

        val pat8 = Atom.parse("你xx", CaseMatching.Smart, Normalization.Smart)
        assertTrue(pat8.ignoreCase)
        assertEquals("你xx", pat8.needle.toString())

        val pat9 = Atom.parse("你xx", CaseMatching.Ignore, Normalization.Smart)
        assertTrue(pat9.ignoreCase)
        assertEquals("你xx", pat9.needle.toString())

        val pat10 = Atom.parse("Ⲽxx", CaseMatching.Smart, Normalization.Smart)
        assertFalse(pat10.ignoreCase)
        assertEquals("Ⲽxx", pat10.needle.toString())

        val pat11 = Atom.parse("Ⲽxx", CaseMatching.Ignore, Normalization.Smart)
        assertTrue(pat11.ignoreCase)
        assertEquals("ⲽxx", pat11.needle.toString())
    }

    @Test
    fun escape() {
        val pat = Atom.parse("foo\\ bar", CaseMatching.Smart, Normalization.Smart)
        assertEquals("foo bar", pat.needle.toString())

        val pat2 = Atom.parse("\\!foo", CaseMatching.Smart, Normalization.Smart)
        assertEquals("!foo", pat2.needle.toString())
        assertEquals(AtomKind.Fuzzy, pat2.kind)

        val pat3 = Atom.parse("\\'foo", CaseMatching.Smart, Normalization.Smart)
        assertEquals("'foo", pat3.needle.toString())
        assertEquals(AtomKind.Fuzzy, pat3.kind)

        val pat4 = Atom.parse("\\^foo", CaseMatching.Smart, Normalization.Smart)
        assertEquals("^foo", pat4.needle.toString())
        assertEquals(AtomKind.Fuzzy, pat4.kind)

        val pat5 = Atom.parse("foo\\$", CaseMatching.Smart, Normalization.Smart)
        assertEquals("foo$", pat5.needle.toString())
        assertEquals(AtomKind.Fuzzy, pat5.kind)

        val pat6 = Atom.parse("^foo\\$", CaseMatching.Smart, Normalization.Smart)
        assertEquals("foo$", pat6.needle.toString())
        assertEquals(AtomKind.Prefix, pat6.kind)

        val pat7 = Atom.parse("\\^foo\\$", CaseMatching.Smart, Normalization.Smart)
        assertEquals("^foo$", pat7.needle.toString())
        assertEquals(AtomKind.Fuzzy, pat7.kind)

        val pat8 = Atom.parse("\\!^foo\\$", CaseMatching.Smart, Normalization.Smart)
        assertEquals("!^foo$", pat8.needle.toString())
        assertEquals(AtomKind.Fuzzy, pat8.kind)

        val pat9 = Atom.parse("!\\^foo\\$", CaseMatching.Smart, Normalization.Smart)
        assertEquals("^foo$", pat9.needle.toString())
        assertEquals(AtomKind.Substring, pat9.kind)
    }

    @Test
    fun testPatternAtoms() {
        assertEquals(
            Pattern.parse("a b", CaseMatching.Ignore, Normalization.Smart).atoms,
            listOf(
                Atom.parse("a", CaseMatching.Ignore, Normalization.Smart),
                Atom.parse("b", CaseMatching.Ignore, Normalization.Smart),
            ),
        )

        assertEquals(
            Pattern.parse("a\n b", CaseMatching.Ignore, Normalization.Smart).atoms,
            listOf(
                Atom.parse("a", CaseMatching.Ignore, Normalization.Smart),
                Atom.parse("b", CaseMatching.Ignore, Normalization.Smart),
            ),
        )

        assertEquals(
            Pattern.parse("  a b\r\n", CaseMatching.Ignore, Normalization.Smart).atoms,
            listOf(
                Atom.parse("a", CaseMatching.Ignore, Normalization.Smart),
                Atom.parse("b", CaseMatching.Ignore, Normalization.Smart),
            ),
        )

        assertEquals(
            Pattern.parse("ほ\u3000げ", CaseMatching.Smart, Normalization.Smart).atoms,
            listOf(
                Atom.parse("ほ", CaseMatching.Smart, Normalization.Smart),
                Atom.parse("げ", CaseMatching.Smart, Normalization.Smart),
            ),
        )
    }
}
