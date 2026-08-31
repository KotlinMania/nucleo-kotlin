// port-lint: source matcher/src/config.rs
package io.github.kotlinmania.nucleo

import io.github.kotlinmania.nucleo.chars.CharClass

/**
 * Configuration data that controls how a matcher behaves.
 */
public data class Config(
    public var delimiterChars: ByteArray = "/,:;|".encodeToByteArray(),
    public var bonusBoundaryWhite: Int = BONUS_BOUNDARY + 2,
    public var bonusBoundaryDelimiter: Int = BONUS_BOUNDARY + 1,
    public var initialCharClass: CharClass = CharClass.Whitespace,
    public var normalize: Boolean = true,
    public var ignoreCase: Boolean = true,
    public var preferPrefix: Boolean = false,
) {
    /**
     * Returns a copy of this configuration.
     */
    public fun clone(): Config = copy(delimiterChars = delimiterChars.copyOf())

    /**
     * Configures the matcher with bonuses appropriate for matching file paths.
     */
    public fun setMatchPaths() {
        delimiterChars = "/:".encodeToByteArray()
        bonusBoundaryWhite = BONUS_BOUNDARY
        initialCharClass = CharClass.Delimiter
    }

    /**
     * Returns a copy of the config with bonuses appropriate for matching file paths.
     */
    public fun matchPaths(): Config {
        val copy =
            this.copy(
                delimiterChars = "/".encodeToByteArray(),
                bonusBoundaryWhite = BONUS_BOUNDARY,
                initialCharClass = CharClass.Delimiter,
            )
        return copy
    }

    /**
     * Calculates the boundary bonus between the previous character class and the current character class.
     */
    public fun bonusFor(prevClass: CharClass, clazz: CharClass): Int {
        if (clazz.ordinal > CharClass.Delimiter.ordinal) {
            when (prevClass) {
                CharClass.Whitespace -> return bonusBoundaryWhite
                CharClass.Delimiter -> return bonusBoundaryDelimiter
                CharClass.NonWord -> return BONUS_BOUNDARY
                else -> {}
            }
        }
        return if (prevClass == CharClass.Lower &&
            clazz == CharClass.Upper ||
            prevClass != CharClass.Number &&
            clazz == CharClass.Number
        ) {
            BONUS_CAMEL123
        } else if (clazz == CharClass.Whitespace) {
            bonusBoundaryWhite
        } else if (clazz == CharClass.NonWord) {
            BONUS_NON_WORD
        } else {
            0
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Config) return false
        if (!delimiterChars.contentEquals(other.delimiterChars)) return false
        if (bonusBoundaryWhite != other.bonusBoundaryWhite) return false
        if (bonusBoundaryDelimiter != other.bonusBoundaryDelimiter) return false
        if (initialCharClass != other.initialCharClass) return false
        if (normalize != other.normalize) return false
        if (ignoreCase != other.ignoreCase) return false
        if (preferPrefix != other.preferPrefix) return false
        return true
    }

    override fun hashCode(): Int {
        var result = delimiterChars.contentHashCode()
        result = 31 * result + bonusBoundaryWhite
        result = 31 * result + bonusBoundaryDelimiter
        result = 31 * result + initialCharClass.hashCode()
        result = 31 * result + normalize.hashCode()
        result = 31 * result + ignoreCase.hashCode()
        result = 31 * result + preferPrefix.hashCode()
        return result
    }

    public companion object {
        public val DEFAULT: Config = Config()
    }
}
