package com.cocos.androidaccounting.util

import com.cocos.androidaccounting.data.model.BillType
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun formatYuan_2300() {
        assertEquals("¥23.00", MoneyFormatter.formatYuan(2300L))
    }

    @Test
    fun formatYuan_0() {
        assertEquals("¥0.00", MoneyFormatter.formatYuan(0L))
    }

    @Test
    fun formatYuan_5() {
        assertEquals("¥0.05", MoneyFormatter.formatYuan(5L))
    }

    @Test
    fun formatSignedYuan_expense() {
        assertEquals("-¥23.00", MoneyFormatter.formatSignedYuan(BillType.EXPENSE, 2300L))
    }

    @Test
    fun formatSignedYuan_income() {
        assertEquals("+¥50.00", MoneyFormatter.formatSignedYuan(BillType.INCOME, 5000L))
    }

    @Test(expected = IllegalArgumentException::class)
    fun formatYuan_negativeThrows() {
        MoneyFormatter.formatYuan(-1L)
    }
}
