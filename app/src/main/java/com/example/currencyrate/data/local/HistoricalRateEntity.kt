package com.example.currencyrate.data.local

import androidx.room.Entity

@Entity(tableName = "historical_rates", primaryKeys = ["code", "date"])
data class HistoricalRateEntity(
    val code: String,
    val date: String,
    val rate: Double
)
