package com.woosung.widgetpractice

import android.content.Context
import android.location.Geocoder
import java.util.Locale

data class WeatherResponse(
    val response: ApiResponse
)

data class ApiResponse(
    val header: ApiHeader,
    val body: ApiBody
)

data class ApiHeader(
    val resultCode: String,
    val resultMsg: String
)

data class ApiBody(
    val dataType: String,
    val items: ApiItems,
    val totalCount: Int
)

data class ApiItems(
    val item: List<ApiItem>
)

data class ApiItem(
    val baseDate: String,
    val baseTime: String,
    val category: String,
    val fcstValue: String,
    val nx: Int,
    val ny: Int,
    val fcstDate: String,
    val fcstTime: String
)
