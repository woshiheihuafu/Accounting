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
