package com.cocos.androidaccounting.data.local.converter

import androidx.room.TypeConverter
import com.cocos.androidaccounting.data.model.BillType
import java.time.Instant
import java.time.LocalDate

class RoomConverters {
    @TypeConverter
    fun billTypeToString(value: BillType): String = value.name

    @TypeConverter
    fun stringToBillType(value: String): BillType = BillType.valueOf(value)

    @TypeConverter
    fun localDateToLong(value: LocalDate): Long = value.toEpochDay()

    @TypeConverter
    fun longToLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter
    fun instantToLong(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun longToInstant(value: Long): Instant = Instant.ofEpochMilli(value)
}
