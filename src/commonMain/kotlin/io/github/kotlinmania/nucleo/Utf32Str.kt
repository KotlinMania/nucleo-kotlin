// port-lint: source utf32_str.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.chars.graphemes

/**
 * Check if a given string can be represented internally as the Ascii variant in a Utf32String or Utf32Str.
 */
public fun hasAsciiGraphemes(string: String): Boolean {
    for (i in 0 until string.length) {
        val c = string[i]
        if (c.code > 127) {
            return false
        }
        if (c == '\r' && i + 1 < string.length && string[i + 1] == '\n') {
            return false
        }
    }
    return true
}

/**
 * A UTF-32 encoded character array/slice used as input for matching.
 */
public sealed class Utf32Str : Comparable<Utf32Str> {
    public abstract val length: Int

    public fun len(): Int = length

    public fun isEmpty(): Boolean = length == 0

    public abstract fun isAscii(): Boolean

    public abstract fun get(n: Int): Char

    public fun get(n: UInt): Char = get(n.toInt())

    public abstract fun last(): Char

    public abstract fun first(): Char

    public abstract fun chars(): Sequence<Char>

    public abstract fun slice(startIndex: Int = 0, endIndex: Int = length): Utf32Str

    public fun sliceU32(startIndex: UInt = 0u, endIndex: UInt = length.toUInt()): Utf32Str =
        slice(startIndex.toInt(), endIndex.toInt())

    public abstract fun leadingWhiteSpace(): Int

    public abstract fun trailingWhiteSpace(): Int

    public class Ascii(
        public val bytes: ByteArray,
        public val offset: Int = 0,
        public val size: Int = bytes.size - offset,
    ) : Utf32Str() {
        override val length: Int get() = size

        override fun isAscii(): Boolean = true

        override fun get(n: Int): Char {
            if (n < 0 || n >= size) throw IndexOutOfBoundsException("Index $n out of bounds for length $size")
            return (bytes[offset + n].toInt() and 0xFF).toChar()
        }

        override fun last(): Char {
            if (size == 0) throw NoSuchElementException("Utf32Str is empty")
            return (bytes[offset + size - 1].toInt() and 0xFF).toChar()
        }

        override fun first(): Char {
            if (size == 0) throw NoSuchElementException("Utf32Str is empty")
            return (bytes[offset].toInt() and 0xFF).toChar()
        }

        override fun chars(): Sequence<Char> =
            sequence {
                for (i in 0 until size) {
                    yield((bytes[offset + i].toInt() and 0xFF).toChar())
                }
            }

        override fun slice(startIndex: Int, endIndex: Int): Utf32Str {
            val s = startIndex.coerceAtLeast(0)
            val e = endIndex.coerceAtMost(size)
            if (s >= e) return Ascii(bytes, offset + s, 0)
            return Ascii(bytes, offset + s, e - s)
        }

        override fun leadingWhiteSpace(): Int {
            for (i in 0 until size) {
                val b = bytes[offset + i].toInt() and 0xFF
                if (b != 0x20 && b != 0x09 && b != 0x0A && b != 0x0C && b != 0x0D) {
                    return i
                }
            }
            return size
        }

        override fun trailingWhiteSpace(): Int {
            for (i in 0 until size) {
                val b = bytes[offset + size - 1 - i].toInt() and 0xFF
                if (b != 0x20 && b != 0x09 && b != 0x0A && b != 0x0C && b != 0x0D) {
                    return i
                }
            }
            return size
        }

        public fun toByteArray(): ByteArray = bytes.copyOfRange(offset, offset + size)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Utf32Str) return false
            if (length != other.length) return false
            for (i in 0 until length) {
                if (get(i) != other.get(i)) return false
            }
            return true
        }

        override fun hashCode(): Int {
            var result = 1
            for (i in 0 until size) {
                result = 31 * result + (bytes[offset + i].toInt() and 0xFF)
            }
            return result
        }

        override fun toString(): String =
            buildString(size) {
                for (i in 0 until size) {
                    append((bytes[offset + i].toInt() and 0xFF).toChar())
                }
            }
    }

    public class Unicode(
        public val codepoints: CharArray,
        public val offset: Int = 0,
        public val size: Int = codepoints.size - offset,
    ) : Utf32Str() {
        override val length: Int get() = size

        override fun isAscii(): Boolean = false

        override fun get(n: Int): Char {
            if (n < 0 || n >= size) throw IndexOutOfBoundsException("Index $n out of bounds for length $size")
            return codepoints[offset + n]
        }

        override fun last(): Char {
            if (size == 0) throw NoSuchElementException("Utf32Str is empty")
            return codepoints[offset + size - 1]
        }

        override fun first(): Char {
            if (size == 0) throw NoSuchElementException("Utf32Str is empty")
            return codepoints[offset]
        }

        override fun chars(): Sequence<Char> =
            sequence {
                for (i in 0 until size) {
                    yield(codepoints[offset + i])
                }
            }

        override fun slice(startIndex: Int, endIndex: Int): Utf32Str {
            val s = startIndex.coerceAtLeast(0)
            val e = endIndex.coerceAtMost(size)
            if (s >= e) return Unicode(codepoints, offset + s, 0)
            return Unicode(codepoints, offset + s, e - s)
        }

        override fun leadingWhiteSpace(): Int {
            for (i in 0 until size) {
                if (!codepoints[offset + i].isWhitespace()) {
                    return i
                }
            }
            return size
        }

        override fun trailingWhiteSpace(): Int {
            for (i in 0 until size) {
                if (!codepoints[offset + size - 1 - i].isWhitespace()) {
                    return i
                }
            }
            return size
        }

        public fun toCharArray(): CharArray = codepoints.copyOfRange(offset, offset + size)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Utf32Str) return false
            if (length != other.length) return false
            for (i in 0 until length) {
                if (get(i) != other.get(i)) return false
            }
            return true
        }

        override fun hashCode(): Int {
            var result = 1
            for (i in 0 until size) {
                result = 31 * result + codepoints[offset + i].hashCode()
            }
            return result
        }

        override fun toString(): String =
            buildString(size) {
                for (i in 0 until size) {
                    append(codepoints[offset + i])
                }
            }
    }

    override fun compareTo(other: Utf32Str): Int {
        val minLen = minOf(length, other.length)
        for (i in 0 until minLen) {
            val cmp = get(i).compareTo(other.get(i))
            if (cmp != 0) return cmp
        }
        return length.compareTo(other.length)
    }

    public companion object {
        public fun new(str: String, buf: MutableList<Char> = mutableListOf()): Utf32Str =
            if (hasAsciiGraphemes(str)) {
                Ascii(str.encodeToByteArray())
            } else {
                buf.clear()
                buf.addAll(graphemes(str))
                Unicode(buf.toCharArray())
            }

        public fun fromAscii(bytes: ByteArray): Utf32Str = Ascii(bytes)

        public fun fromUnicode(chars: CharArray): Utf32Str = Unicode(chars)
    }
}

/**
 * An owned version of [Utf32Str].
 */
public sealed class Utf32String : Comparable<Utf32String> {
    public abstract val length: Int

    public fun len(): Int = length

    public fun isEmpty(): Boolean = length == 0

    public abstract fun slice(startIndex: Int = 0, endIndex: Int = length): Utf32Str

    public fun sliceU32(startIndex: UInt = 0u, endIndex: UInt = length.toUInt()): Utf32Str =
        slice(startIndex.toInt(), endIndex.toInt())

    public class Ascii(
        public val text: String,
    ) : Utf32String() {
        private val bytes: ByteArray = text.encodeToByteArray()

        override val length: Int get() = bytes.size

        override fun slice(startIndex: Int, endIndex: Int): Utf32Str {
            val s = startIndex.coerceAtLeast(0)
            val e = endIndex.coerceAtMost(length)
            if (s >= e) return Utf32Str.Ascii(bytes, s, 0)
            return Utf32Str.Ascii(bytes, s, e - s)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Utf32String) return false
            return toString() == other.toString()
        }

        override fun hashCode(): Int = text.hashCode()

        override fun toString(): String = text
    }

    public class Unicode(
        public val codepoints: CharArray,
    ) : Utf32String() {
        override val length: Int get() = codepoints.size

        override fun slice(startIndex: Int, endIndex: Int): Utf32Str {
            val s = startIndex.coerceAtLeast(0)
            val e = endIndex.coerceAtMost(length)
            if (s >= e) return Utf32Str.Unicode(codepoints, s, 0)
            return Utf32Str.Unicode(codepoints, s, e - s)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Utf32String) return false
            return toString() == other.toString()
        }

        override fun hashCode(): Int = codepoints.contentHashCode()

        override fun toString(): String = codepoints.concatToString()
    }

    override fun compareTo(other: Utf32String): Int = toString().compareTo(other.toString())

    public companion object {
        public fun from(value: String): Utf32String =
            if (hasAsciiGraphemes(value)) {
                Ascii(value)
            } else {
                Unicode(graphemes(value).toList().toCharArray())
            }
    }
}
