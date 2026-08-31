// port-lint: source matcher/src/debug.rs
package io.github.kotlinmania.nucleo

/**
 * Formats a [ScoreCell] into a debug representation matching upstream debug formatting.
 */
internal fun ScoreCell.debugString(): String = "($score, $matched)"

internal fun fmt(cell: ScoreCell): String = cell.debugString()

/**
 * Formats a [MatrixCell] into a debug representation matching upstream debug formatting.
 */
internal fun MatrixCell.debugString(): String = "(${get(false)}, ${get(true)})"

internal fun fmt(cell: MatrixCell): String = cell.debugString()
