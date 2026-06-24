package com.cocos.androidaccounting.util

import com.cocos.androidaccounting.data.model.BillType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    // appendDigit 测试
    @Test fun appendDigit_basic() = assertEquals("123", appendDigit("12", 3))

    @Test fun appendDigit_leadingZeroReplaced() {
        assertEquals("5", appendDigit("0", 5))
        assertEquals("0", appendDigit("0", 0))
        assertEquals("0", appendDigit("", 0))
    }

    @Test fun appendDigit_maxDecimalIgnored() = assertEquals("1.23", appendDigit("1.23", 4))

    @Test fun appendDigit_maxIntegerIgnored() = assertEquals("9999999", appendDigit("9999999", 9))

    // appendDot 测试
    @Test fun appendDot_emptyInput() = assertEquals("0.", appendDot(""))

    @Test fun appendDot_normal() = assertEquals("12.", appendDot("12"))

    @Test fun appendDot_duplicateIgnored() = assertEquals("1.2", appendDot("1.2"))

    // amountInputToCents 测试
    @Test fun amountInputToCents_normal() {
        assertEquals(1230L, amountInputToCents("12.30"))
        assertEquals(500L, amountInputToCents("5"))
        assertEquals(5L, amountInputToCents("0.05"))
    }

    @Test fun amountInputToCents_invalid() {
        assertNull(amountInputToCents(""))
        assertNull(amountInputToCents("0"))
        assertNull(amountInputToCents("."))
        assertNull(amountInputToCents("0."))
    }
}
