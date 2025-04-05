package com.woosung.widgetpractice

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.location.Geocoder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Implementation of App Widget functionality.
 */
class rainandsnow : AppWidgetProvider() {
    val apiKey = "CGYBlJQVqP6w3Ab+6xNR7mIFEnyd5LepVXiO+UtXWO/LZ1ijGxtPEfzn08cObtTMKC1bwkQYwIpLLNO3xdMA8w=="

    @SuppressLint("MissingPermission")
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                Log.e("LOCATION", "위치 정보를 가져올 수 없습니다.")
                return@addOnSuccessListener
            }
            location.let {
                val lat = it.latitude
                val lon = it.longitude
                val (nx, ny) = GridConverter.convertGRID_GPS(lat, lon)

                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MINUTE, -30)
                val baseDate = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(calendar.time)
                val baseTime = SimpleDateFormat("HH00", Locale.KOREA).format(calendar.time)

                RetrofitClient.api.getRealtimeWeather(apiKey, baseDate = baseDate, baseTime = baseTime, nx = nx, ny = ny)
                    .enqueue(object : Callback<WeatherResponse> {
                        override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                            val items = response.body()?.response?.body?.items?.item ?: return
                            Log.d("API_RESPONSE", "items = $items")  // 추가
                            val temp = items.find { it.category == "T1H" }?.obsrValue ?: "N/A"
                            val weatherStatus = getWeatherStatus(items)
                            val cityName = getCityName(context, lat, lon)

                            updateWidget(context, appWidgetManager, appWidgetIds, cityName, temp, weatherStatus)
                        }

                        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                            Log.e("API_ERROR", "API 호출 실패: ${t.message}")
                        }

                    })
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray, city: String, temp: String, status: String) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.weather_widget_layout)
            views.setTextViewText(R.id.cityTextView, city)
            views.setTextViewText(R.id.tempTextView, "$temp ℃")
            views.setTextViewText(R.id.statusTextView, status)
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun getWeatherStatus(items: List<ApiItem>): String {
        val sky = items.find { it.category == "SKY" }?.obsrValue ?: "0"
        val pty = items.find { it.category == "PTY" }?.obsrValue ?: "0"

        return when (pty) {
            "1" -> "비"
            "2" -> "비/눈"
            "3" -> "눈"
            "5" -> "빗방울"
            "6" -> "진눈깨비"
            "7" -> "눈날림"
            else -> {
                when (sky) {
                    "1" -> "맑음"
                    "3" -> "구름많음"
                    "4" -> "흐림"
                    else -> "정보 없음"
                }
            }
        }
    }

    private fun getCityName(context: Context, lat: Double, lon: Double): String {
        val geocoder = Geocoder(context, Locale.KOREA)
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        return if (!addresses.isNullOrEmpty()) {
            addresses[0].locality ?: addresses[0].subLocality ?: "알 수 없음"
        } else "알 수 없음"
    }
}
