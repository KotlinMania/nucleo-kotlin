// port-lint: tests pattern/tests.rs
package io.github.kotlinmania.nucleo.pattern

import io.github.kotlinmania.nucleo.Matcher
import io.github.kotlinmania.nucleo.Utf32String
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MultiPatternTest {
    @Test
    fun append() {
        val pat = MultiPattern(1)
        pat.reparse(0, "!", CaseMatching.Smart, Normalization.Smart, true)
        assertEquals(Status.Update, pat.status())
        pat.reparse(0, "!f", CaseMatching.Smart, Normalization.Smart, true)
        assertEquals(Status.Update, pat.status())
        pat.reparse(0, "!fo", CaseMatching.Smart, Normalization.Smart, true)
        assertEquals(Status.Rescore, pat.status())
    }

    @Test
    fun multiColumnScore() {
        val pat = MultiPattern(2)
        pat.reparse(0, "foo", CaseMatching.Smart, Normalization.Smart, false)
        pat.reparse(1, "bar", CaseMatching.Smart, Normalization.Smart, false)

        val matcher = Matcher()
        val matchCols =
            listOf(
                Utf32String.fromAscii("foobar"),
                Utf32String.fromAscii("barbaz"),
            )
        val score = pat.score(matchCols, matcher)
        assertNotNull(score)
        assertTrue(score > 0L)

        val nonMatchCols =
            listOf(
                Utf32String.fromAscii("baz"),
                Utf32String.fromAscii("barbaz"),
            )
        val nonMatchScore = pat.score(nonMatchCols, matcher)
        assertNull(nonMatchScore)
    }

    @Test
    fun cloneAndResetStatus() {
        val pat = MultiPattern(1)
        assertTrue(pat.isEmpty())
        pat.reparse(0, "hello", CaseMatching.Smart, Normalization.Smart, false)
        assertFalse(pat.isEmpty())
        assertEquals(Status.Rescore, pat.status())

        val cloned = pat.clone()
        assertEquals(Status.Rescore, cloned.status())
        cloned.resetStatus()
        assertEquals(Status.Unchanged, cloned.status())
        assertEquals(Status.Rescore, pat.status())
    }
}
