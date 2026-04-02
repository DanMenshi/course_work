package com.example.currencyrate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey val code: String,
    val cbrId: String,
    val name: String,
    val rate: Double,
    val previousRate: Double = 0.0, // НОВОЕ ПОЛЕ: Предыдущий курс
    val nominal: Int,
    val isFavorite: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)