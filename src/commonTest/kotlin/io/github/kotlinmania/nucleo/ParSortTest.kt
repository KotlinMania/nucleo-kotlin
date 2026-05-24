// port-lint: ignore
// Exercises the common Kotlin sort port; upstream keeps par_sort tests implicit in worker behavior.
package io.github.kotlinmania.nucleo

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParSortTest {
    @Test
    fun sortsDescendingInput() {
        val values = MutableList(256) { 255 - it }

        val canceled = parQuicksort(values, { left, right -> left < right }, AtomicBoolean(false))

        assertFalse(canceled)
        assertEquals((0 until 256).toList(), values)
    }

    @Test
    fun preservesComparatorOrderForDuplicateKeys() {
        val values = mutableListOf(4, 1, 3, 1, 2, 3, 2, 4)

        val canceled = parQuicksort(values, { left, right -> left < right }, AtomicBoolean(false))

        assertFalse(canceled)
        assertEquals(listOf(1, 1, 2, 2, 3, 3, 4, 4), values)
    }

    @Test
    fun returnsCanceledBeforeSorting() {
        val values = mutableListOf(3, 2, 1)

        val canceled = parQuicksort(values, { left, right -> left < right }, AtomicBoolean(true))

        assertTrue(canceled)
        assertEquals(listOf(3, 2, 1), values)
    }
}
