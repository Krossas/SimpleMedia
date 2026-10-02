package com.simplemedia.app.util

private data class NaturalToken(
    val value: String,
    val isDigit: Boolean
)

object NaturalSort {
    fun sort(values: List<String>): List<String> {
        return values.sortedWith { left, right ->
            compareNatural(left, right)
        }
    }

    private fun compareNatural(left: String, right: String): Int {
        val leftTokens = tokenize(left)
        val rightTokens = tokenize(right)

        val max = maxOf(leftTokens.size, rightTokens.size)
        for (index in 0 until max) {
            val leftToken = leftTokens.getOrNull(index)
            val rightToken = rightTokens.getOrNull(index)

            if (leftToken == null) return -1
            if (rightToken == null) return 1

            val comparison = when {
                leftToken.isDigit && rightToken.isDigit ->
                    leftToken.value.toLong().compareTo(rightToken.value.toLong())

                leftToken.isDigit != rightToken.isDigit -> {
                    if (leftToken.isDigit) -1 else 1
                }

                else -> leftToken.value.compareTo(rightToken.value, ignoreCase = true)
            }

            if (comparison != 0) return comparison
        }

        return 0
    }

    private fun tokenize(value: String): List<NaturalToken> {
        val pattern = Regex("(\\d+|\\D+)")
        return pattern.findAll(value)
            .map { match ->
                val token = match.value
                NaturalToken(token, token.all(Char::isDigit))
            }
            .toList()
    }
}
