package com.cocos.androidaccounting.data.model

import androidx.annotation.DrawableRes
import com.cocos.androidaccounting.R

data class Category(
    val name: String,
    val type: BillType,
    @param:DrawableRes val iconRes: Int,
)

object Categories {
    val expense: List<Category> = listOf(
        Category("餐饮", BillType.EXPENSE, R.drawable.ic_category_dining),
        Category("购物", BillType.EXPENSE, R.drawable.ic_category_shopping),
        Category("娱乐", BillType.EXPENSE, R.drawable.ic_category_entertainment),
        Category("日用", BillType.EXPENSE, R.drawable.ic_category_daily),
    )
    val income: List<Category> = listOf(
        Category("工资", BillType.INCOME, R.drawable.ic_category_salary),
        Category("理财", BillType.INCOME, R.drawable.ic_category_finance),
        Category("礼金", BillType.INCOME, R.drawable.ic_category_gift),
        Category("其他", BillType.INCOME, R.drawable.ic_category_other),
    )

    fun byType(type: BillType): List<Category> =
        if (type == BillType.EXPENSE) expense else income

    // 兜底图标：未知类目（例如历史数据）使用「其他」图标
    @DrawableRes
    private val fallbackIconRes: Int = R.drawable.ic_category_other

    private val iconByName: Map<String, Int> =
        (expense + income).associate { it.name to it.iconRes }

    /** 按类目名查找图标资源，未命中返回兜底图标。 */
    @DrawableRes
    fun iconResFor(categoryName: String): Int =
        iconByName[categoryName] ?: fallbackIconRes
}
