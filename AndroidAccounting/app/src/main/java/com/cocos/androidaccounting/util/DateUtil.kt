package com.cocos.androidaccounting.util

import java.time.LocalDate
import java.time.YearMonth

object DateUtil {
    fun monthStartEpochDay(yearMonth: YearMonth): Long =
        yearMonth.atDay(1).toEpochDay()

    fun monthEndExclusiveEpochDay(yearMonth: YearMonth): Long =
        yearMonth.plusMonths(1).atDay(1).toEpochDay()

    fun formatGroupHeader(date: LocalDate): String {
        val weekday = weekdayLabel(date)
        return "${date.monthValue}月${date.dayOfMonth}日 $weekday"
    }

    fun formatMonthLabel(yearMonth: YearMonth): String = "${yearMonth.monthValue}月"

    fun weekdayLabel(date: LocalDate): String {
        return when (date.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "周一"
            java.time.DayOfWeek.TUESDAY -> "周二"
            java.time.DayOfWeek.WEDNESDAY -> "周三"
            java.time.DayOfWeek.THURSDAY -> "周四"
            java.time.DayOfWeek.FRIDAY -> "周五"
            java.time.DayOfWeek.SATURDAY -> "周六"
            java.time.DayOfWeek.SUNDAY -> "周日"
        }
    }
}

fun formatRecordDate(date: LocalDate): String =
    "${date.monthValue}月${date.dayOfMonth}日"
