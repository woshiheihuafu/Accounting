package com.cocos.androidaccounting.data.model

import java.time.Instant
import java.time.LocalDate

data class Bill(
    val id: Long = 0L,
    val type: BillType,
    val category: String,
    val amount: Long,
    val date: LocalDate,
    val remark: String = "",
    val createdAt: Instant,
)
