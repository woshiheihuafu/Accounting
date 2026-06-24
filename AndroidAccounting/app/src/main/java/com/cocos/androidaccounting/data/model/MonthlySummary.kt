package com.cocos.androidaccounting.data.model

data class MonthlySummary(
    val income: Long = 0L,
    val expense: Long = 0L,
) {
    val balance: Long get() = income - expense
}
