package com.cocos.androidaccounting.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cocos.androidaccounting.data.model.BillType
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: BillType,
    val category: String,
    val amount: Long,
    val date: LocalDate,
    val remark: String,
    val createdAt: Instant,
)
