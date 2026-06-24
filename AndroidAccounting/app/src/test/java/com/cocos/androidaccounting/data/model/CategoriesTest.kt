package com.cocos.androidaccounting.data.model

import com.cocos.androidaccounting.R
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoriesTest {

    @Test
    fun iconResFor_expenseCategories_returnsDesignIcons() {
        assertEquals(R.drawable.ic_category_dining, Categories.iconResFor("餐饮"))
        assertEquals(R.drawable.ic_category_shopping, Categories.iconResFor("购物"))
        assertEquals(R.drawable.ic_category_entertainment, Categories.iconResFor("娱乐"))
        assertEquals(R.drawable.ic_category_daily, Categories.iconResFor("日用"))
    }

    @Test
    fun iconResFor_incomeCategories_returnsDesignIcons() {
        assertEquals(R.drawable.ic_category_salary, Categories.iconResFor("工资"))
        assertEquals(R.drawable.ic_category_finance, Categories.iconResFor("理财"))
        assertEquals(R.drawable.ic_category_gift, Categories.iconResFor("礼金"))
        assertEquals(R.drawable.ic_category_other, Categories.iconResFor("其他"))
    }

    @Test
    fun iconResFor_unknownCategory_returnsFallback() {
        assertEquals(R.drawable.ic_category_other, Categories.iconResFor("未知类目"))
    }

    @Test
    fun iconResFor_matchesCategoryIconRes() {
        (Categories.expense + Categories.income).forEach { category ->
            assertEquals(category.iconRes, Categories.iconResFor(category.name))
        }
    }
}
