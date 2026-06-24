package com.cocos.androidaccounting.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DateUtilTest {

    @Test
    fun formatGroupHeader_tuesdayJune16() {
        val date = LocalDate.of(2026, 6, 16)
        assertEquals("6月16日 周二", DateUtil.formatGroupHeader(date))
    }

    @Test
    fun formatMonthLabel_june2026() {
        val yearMonth = YearMonth.of(2026, 6)
        assertEquals("6月", DateUtil.formatMonthLabel(yearMonth))
    }

    @Test
    fun weekdayLabel_allDays() {
        val monday = LocalDate.of(2026, 6, 15)
        assertEquals("周一", DateUtil.weekdayLabel(monday))
        assertEquals("周二", DateUtil.weekdayLabel(monday.plusDays(1)))
        assertEquals("周三", DateUtil.weekdayLabel(monday.plusDays(2)))
        assertEquals("周四", DateUtil.weekdayLabel(monday.plusDays(3)))
        assertEquals("周五", DateUtil.weekdayLabel(monday.plusDays(4)))
        assertEquals("周六", DateUtil.weekdayLabel(monday.plusDays(5)))
        assertEquals("周日", DateUtil.weekdayLabel(monday.plusDays(6)))
    }
}
