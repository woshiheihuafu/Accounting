package com.cocos.androidaccounting.util

import com.cocos.androidaccounting.data.model.BillType
import java.math.BigDecimal
import java.math.RoundingMode

object MoneyFormatter {
    fun formatYuan(cents: Long): String {
        require(cents >= 0) { "cents must be non-negative, got $cents" }
        return "¥" + BigDecimal(cents).movePointLeft(2).setScale(2, RoundingMode.HALF_UP).toPlainString()
    }

    fun formatSignedYuan(type: BillType, cents: Long): String {
        val sign = if (type == BillType.EXPENSE) "-" else "+"
        return sign + formatYuan(cents)
    }
}

fun appendDigit(input: String, n: Int): String {
    return when {
        input == "0" && n != 0 -> n.toString()
        input == "0" && n == 0 -> "0"
        input.contains('.') && input.substringAfter('.').length >= 2 -> input
        !input.contains('.') && input.length >= 7 -> input
        else -> {
            val result = input + n
            val parsed = result.toBigDecimalOrNull() ?: return input
            if (parsed > java.math.BigDecimal("9999999.99")) input else result
        }
    }
}

fun appendDot(input: String): String = when {
    input.contains('.') -> input
    input.isEmpty() -> "0."
    else -> "$input."
}

fun amountInputToCents(input: String): Long? =
    runCatching {
        java.math.BigDecimal(input)
            .movePointRight(2)
            .setScale(0, java.math.RoundingMode.HALF_UP)
            .toLong()
    }.getOrNull()?.takeIf { it > 0L }
