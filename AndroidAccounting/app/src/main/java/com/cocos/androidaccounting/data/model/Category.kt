package com.cocos.androidaccounting.data.model

import androidx.annotation.DrawableRes

data class Category(
    val name: String,
    val type: BillType,
    @param:DrawableRes val iconRes: Int,
)

object Categories {
    val expense: List<Category> = listOf(
        Category("餐饮", BillType.EXPENSE, android.R.drawable.ic_menu_agenda),
        Category("购物", BillType.EXPENSE, android.R.drawable.ic_menu_gallery),
        Category("娱乐", BillType.EXPENSE, android.R.drawable.ic_menu_slideshow),
        Category("日用", BillType.EXPENSE, android.R.drawable.ic_menu_edit),
    )
    val income: List<Category> = listOf(
        Category("工资", BillType.INCOME, android.R.drawable.ic_menu_send),
        Category("理财", BillType.INCOME, android.R.drawable.ic_menu_info_details),
        Category("礼金", BillType.INCOME, android.R.drawable.ic_menu_share),
        Category("其他", BillType.INCOME, android.R.drawable.ic_menu_add),
    )

    fun byType(type: BillType): List<Category> =
        if (type == BillType.EXPENSE) expense else income
}
