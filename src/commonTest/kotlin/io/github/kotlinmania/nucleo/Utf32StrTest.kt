// port-lint: tests utf32_str/tests.rs
package io.github.kotlinmania.nucleo

import kotlin.test.Test
import kotlin.test.assertEquals

class Utf32StrTest {
    private fun expectAscii(src: String, isAscii: Boolean) {
        val buffer = mutableListOf<Char>()
        assertEquals(isAscii, Utf32Str.new(src, buffer).isAscii())
        assertEquals(isAscii, Utf32String.from(src).slice().isAscii())
    }

    @Test
    fun testUtf32strAscii() {
        // ascii
        expectAscii("", true)
        expectAscii("a", true)
        expectAscii("a\nb", true)
        expectAscii("\n\r", true)

        // not ascii
        expectAscii("aü", false)
        expectAscii("au\u0308", false)

        // windows-style newline
        expectAscii("a\r\nb", false)
        expectAscii("ü\r\n", false)
        expectAscii("\r\n", false)
    }

    @Test
    fun testGraphemeTruncation() {
        // ascii is preserved
        val s = Utf32String.from("ab")
        assertEquals('a', s.slice().get(0))
        assertEquals('b', s.slice().get(1))

        // windows-style newline is truncated to '\n'
        val s2 = Utf32String.from("\r\n")
        assertEquals('\n', s2.slice().get(0))

        // normal graphemes are truncated to the first character
        val s3 = Utf32String.from("u\u0308\r\n")
        assertEquals('u', s3.slice().get(0))
        assertEquals('\n', s3.slice().get(1))
    }
}
