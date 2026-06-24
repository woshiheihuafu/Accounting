package com.cocos.androidaccounting.data.mapper

import com.cocos.androidaccounting.data.local.BillEntity
import com.cocos.androidaccounting.data.model.Bill

fun BillEntity.toDomain(): Bill = Bill(
    id = id,
    type = type,
    category = category,
    amount = amount,
    date = date,
    remark = remark,
    createdAt = createdAt,
)

fun List<BillEntity>.toDomainList(): List<Bill> = map { it.toDomain() }

fun Bill.toEntity(): BillEntity = BillEntity(
    id = id,
    type = type,
    category = category,
    amount = amount,
    date = date,
    remark = remark,
    createdAt = createdAt,
)
