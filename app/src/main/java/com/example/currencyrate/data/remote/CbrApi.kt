package com.example.currencyrate.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface CbrApi {
    @GET("scripts/XML_daily.asp")
    suspend fun getDailyCurrencies(): ValCurs

    @GET("scripts/XML_dynamic.asp")
    suspend fun getHistoricalRates(
        @Query("date_req1") dateStart: String,
        @Query("date_req2") dateEnd: String,
        @Query("VAL_NM_RQ") valuteId: String
    ): ValCurs
}
