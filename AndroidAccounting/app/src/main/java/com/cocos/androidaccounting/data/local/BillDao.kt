package com.cocos.androidaccounting.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cocos.androidaccounting.data.model.BillType
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Query("""
        SELECT * FROM bills
        WHERE date >= :startEpochDay AND date < :endEpochDayExclusive
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeBillsByMonth(
        startEpochDay: Long,
        endEpochDayExclusive: Long,
    ): Flow<List<BillEntity>>

    @Query("""
        SELECT SUM(amount) FROM bills
        WHERE type = :type
          AND date >= :startEpochDay AND date < :endEpochDayExclusive
    """)
    fun observeSumByType(
        type: BillType,
        startEpochDay: Long,
        endEpochDayExclusive: Long,
    ): Flow<Long?>

    @Insert
    suspend fun insert(entity: BillEntity): Long

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteById(id: Long)
}
