// port-lint: tests matcher/src/chars/normalize.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.chars.normalize
import kotlin.test.Test
import kotlin.test.assertEquals

class NormalizeTest {
    private fun checkConversions(pairs: List<Pair<Char, Char>>) {
        for ((original, normalized) in pairs) {
            assertEquals(normalized, normalize(original))
        }
    }

    @Test
    fun general() {
        checkConversions(
            listOf(
                Pair('ą', 'a'),
                Pair('À', 'A'),
                Pair('ć', 'c'),
                Pair('ę', 'e'),
                Pair('ł', 'l'),
                Pair('ń', 'n'),
                Pair('ó', 'o'),
                Pair('ś', 's'),
                Pair('ź', 'z'),
                Pair('ż', 'z'),
                Pair('Ą', 'A'),
                Pair('Ć', 'C'),
                Pair('Ę', 'E'),
                Pair('ł', 'l'),
                Pair('Ł', 'L'),
                Pair('Ń', 'N'),
                Pair('Ó', 'O'),
                Pair('Ś', 'S'),
                Pair('Ź', 'Z'),
                Pair('Ż', 'Z'),
                Pair('¡', '!'),
            ),
        )
    }

    @Test
    fun invisibleChars() {
        checkConversions(
            listOf(
                Pair('\u00a0', '\u00a0'),
                Pair('\u00ad', '\u00ad'),
            ),
        )
    }

    @Test
    fun boundaryCases() {
        checkConversions(
            listOf(
                Pair('\u009f', '\u009f'),
                Pair('\u00a0', '\u00a0'),
                Pair('¡', '!'),
                Pair('ʟ', 'L'),
                Pair('\u02a0', '\u02a0'),
                Pair('\u1dff', '\u1dff'),
                Pair('Ḁ', 'A'),
                Pair('ỹ', 'y'),
                Pair('\u1eff', '\u1eff'),
                Pair('\u1f00', '\u1f00'),
                Pair('⁰', '0'),
                Pair('\u209c', 't'),
                Pair('\u209f', '\u209f'),
                Pair('\u20a0', '\u20a0'),
            ),
        )
    }

    @Test
    fun unchangedOutsideBlocks() {
        checkConversions(
            listOf(
                Pair('a', 'a'),
                Pair('⟁', '⟁'),
                Pair('┍', '┍'),
                Pair('ω', 'ω'),
                Pair('⁕', '⁕'),
                Pair('ה', 'ה'),
            ),
        )
    }
}
