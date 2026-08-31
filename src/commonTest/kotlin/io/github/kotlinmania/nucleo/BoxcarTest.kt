// port-lint: tests src/boxcar.rs
package io.github.kotlinmania.nucleo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BoxcarTest {
    @Test
    fun location() {
        assertEquals(32, Location.bucketLen(0))
        for (i in 0u until 32u) {
            val loc = Location.of(i)
            assertEquals(32, loc.bucketLen)
            assertEquals(0, loc.bucket)
            assertEquals(i.toInt(), loc.entry)
        }

        assertEquals(64, Location.bucketLen(1))
        for (i in 33u until 96u) {
            val loc = Location.of(i)
            assertEquals(64, loc.bucketLen)
            assertEquals(1, loc.bucket)
            assertEquals((i - 32u).toInt(), loc.entry)
        }

        assertEquals(128, Location.bucketLen(2))
        for (i in 96u until 224u) {
            val loc = Location.of(i)
            assertEquals(128, loc.bucketLen)
            assertEquals(2, loc.bucket)
            assertEquals((i - 96u).toInt(), loc.entry)
        }
    }

    @Test
    fun extendUniqueBucket() {
        val vec = BoxcarVec.withCapacity<UInt>(1u, 1u)
        vec.extend(0u until 10u) { _, _ -> }
        assertEquals(10u, vec.count())
        for (i in 0u until 10u) {
            assertEquals(i, vec.get(i)?.data)
        }
        assertNull(vec.get(10u))
    }

    @Test
    fun extendOverTwoBuckets() {
        val vec = BoxcarVec.withCapacity<UInt>(1u, 1u)
        vec.extend(0u until 100u) { _, _ -> }
        assertEquals(100u, vec.count())
        for (i in 0u until 100u) {
            assertEquals(i, vec.get(i)?.data)
        }
        assertNull(vec.get(100u))
    }

    @Test
    fun extendOverMoreThanTwoBuckets() {
        val vec = BoxcarVec.withCapacity<UInt>(1u, 1u)
        vec.extend(0u until 1000u) { _, _ -> }
        assertEquals(1000u, vec.count())
        for (i in 0u until 1000u) {
            assertEquals(i, vec.get(i)?.data)
        }
        assertNull(vec.get(1000u))
    }

    @Test
    fun extendWithIncorrectReportedLenIsCaught() {
        val vec = BoxcarVec.withCapacity<UInt>(1u, 1u)
        val iter = IncorrectLenIter(10, (0u until 12u).iterator())
        vec.extend(Iterable { iter }) { _, _ -> }
        assertEquals(12u, vec.count())
    }

    @Test
    fun extendOverMaxCapacity() {
        val vec = BoxcarVec.withCapacity<UInt>(1u, 1u)
        assertEquals(0u, vec.count())
    }

    private class IncorrectLenIter(
        val len: Int,
        private val iter: Iterator<UInt>,
    ) : Iterator<UInt> {
        override fun hasNext(): Boolean = iter.hasNext()

        override fun next(): UInt = iter.next()
    }
}
