// port-lint: tests tests.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.pattern.CaseMatching
import io.github.kotlinmania.nucleo.pattern.Normalization
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NucleoTest {
    @Test
    fun activeInjectorCount() {
        val nucleo = Nucleo<Unit>(Config.DEFAULT, {}, 1, 1u)
        assertEquals(0, nucleo.activeInjectors())
        val injector = nucleo.injector()
        assertEquals(1, nucleo.activeInjectors())
        val injector2 = nucleo.injector()
        assertEquals(2, nucleo.activeInjectors())
        injector2.close()
        assertEquals(1, nucleo.activeInjectors())
        nucleo.restart(false)
        assertEquals(0, nucleo.activeInjectors())
        val injector3 = nucleo.injector()
        assertEquals(1, nucleo.activeInjectors())
        nucleo.tick(0u)
        assertEquals(1, nucleo.activeInjectors())
        injector.close()
        assertEquals(1, nucleo.activeInjectors())
        injector3.close()
        assertEquals(0, nucleo.activeInjectors())
    }

    @Test
    fun testInjectionAndMatching() {
        var notified = false
        val nucleo = Nucleo<String>(Config.DEFAULT, { notified = true }, columns = 1u)
        val injector = nucleo.injector()

        val idx0 =
            injector.push("src/main.rs") { str, cols ->
                cols[0] = Utf32String.fromAscii(str)
            }
        val idx1 =
            injector.push("src/lib.rs") { str, cols ->
                cols[0] = Utf32String.fromAscii(str)
            }
        val idx2 =
            injector.push("README.md") { str, cols ->
                cols[0] = Utf32String.fromAscii(str)
            }

        assertEquals(0u, idx0)
        assertEquals(1u, idx1)
        assertEquals(2u, idx2)
        assertEquals(3u, injector.injectedItems())
        assertTrue(notified)

        // Empty pattern matches all items
        val status1 = nucleo.tick()
        assertTrue(status1.changed)
        assertFalse(status1.running)
        val snap1 = nucleo.snapshot()
        assertEquals(3u, snap1.itemCount())
        assertEquals(3u, snap1.matchedItemCount())
        assertEquals(3, snap1.matchedItems().size)

        // Reparse pattern for "main"
        nucleo.pattern.reparse(0, "main", CaseMatching.Smart, Normalization.Smart, false)
        val status2 = nucleo.tick()
        assertTrue(status2.changed)
        val snap2 = nucleo.snapshot()
        assertEquals(3u, snap2.itemCount())
        assertEquals(1u, snap2.matchedItemCount())
        val matchItem = snap2.getMatchedItem(0u)
        assertNotNull(matchItem)
        assertEquals("src/main.rs", matchItem.data)

        // Non-existent match index returns null
        assertNull(snap2.getMatchedItem(1u))
    }

    @Test
    fun testMultiColumnMatching() {
        val nucleo = Nucleo<Pair<String, String>>(Config.DEFAULT, columns = 2u)
        val injector = nucleo.injector()

        injector.push("Helix" to "editor") { pair, cols ->
            cols[0] = Utf32String.fromAscii(pair.first)
            cols[1] = Utf32String.fromAscii(pair.second)
        }
        injector.push("Rust" to "language") { pair, cols ->
            cols[0] = Utf32String.fromAscii(pair.first)
            cols[1] = Utf32String.fromAscii(pair.second)
        }

        nucleo.pattern.reparse(0, "hel", CaseMatching.Smart, Normalization.Smart, false)
        nucleo.pattern.reparse(1, "edit", CaseMatching.Smart, Normalization.Smart, false)

        nucleo.tick()
        val snap = nucleo.snapshot()
        assertEquals(1u, snap.matchedItemCount())
        assertEquals("Helix", snap.getMatchedItem(0u)?.data?.first)
    }

    @Test
    fun testSortingAndReversal() {
        val nucleo = Nucleo<String>(Config.DEFAULT, columns = 1u)
        val injector = nucleo.injector()

        injector.extend(listOf("a", "aa", "aaa")) { str, cols ->
            cols[0] = Utf32String.fromAscii(str)
        }

        nucleo.pattern.reparse(0, "a", CaseMatching.Smart, Normalization.Smart, false)
        nucleo.tick()
        val snap = nucleo.snapshot()
        assertEquals(3u, snap.matchedItemCount())
        // Shorter length wins tie-breaker when score is equal
        assertEquals("a", snap.getMatchedItem(0u)?.data)
        assertEquals("aa", snap.getMatchedItem(1u)?.data)
        assertEquals("aaa", snap.getMatchedItem(2u)?.data)
    }
}
