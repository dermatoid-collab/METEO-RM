package it.meteoapp.clone.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleWeatherApi {

    /**
     * Current conditions
     * GET https://weather.googleapis.com/v1/currentConditions:lookup
     */
    @GET("v1/currentConditions:lookup")
    suspend fun getCurrentConditions(
        @Query("location.latitude")  lat: Double,
        @Query("location.longitude") lon: Double,
        @Query("key")                apiKey: String,
        @Query("languageCode")       lang: String = "it"
    ): GoogleCurrentResponse

    /**
     * Hourly forecast — fino a 240 ore
     * GET https://weather.googleapis.com/v1/forecast/hours:lookup
     */
    @GET("v1/forecast/hours:lookup")
    suspend fun getHourlyForecast(
        @Query("location.latitude")  lat: Double,
        @Query("location.longitude") lon: Double,
        @Query("key")                apiKey: String,
        @Query("hours")              hours: Int = 48,
        @Query("languageCode")       lang: String = "it"
    ): GoogleHourlyResponse

    /**
     * Daily forecast — fino a 10 giorni
     * GET https://weather.googleapis.com/v1/forecast/days:lookup
     */
    @GET("v1/forecast/days:lookup")
    suspend fun getDailyForecast(
        @Query("location.latitude")  lat: Double,
        @Query("location.longitude") lon: Double,
        @Query("key")                apiKey: String,
        @Query("days")               days: Int = 7,
        @Query("languageCode")       lang: String = "it"
    ): GoogleDailyResponse
}

// ── Response models ───────────────────────────────────────────────────────────

data class GoogleCurrentResponse(
    @SerializedName("currentConditions") val conditions: GoogleCurrentConditions?
)

data class GoogleCurrentConditions(
    @SerializedName("time")             val time: String,
    @SerializedName("isDaytime")        val isDaytime: Boolean,
    @SerializedName("weatherCondition") val weatherCondition: GoogleWeatherCondition?,
    @SerializedName("temperature")      val temperature: GoogleTemperatureValue?,
    @SerializedName("feelsLikeTemperature") val feelsLike: GoogleTemperatureValue?,
    @SerializedName("dewPoint")         val dewPoint: GoogleTemperatureValue?,
    @SerializedName("heatIndex")        val heatIndex: GoogleTemperatureValue?,
    @SerializedName("windChill")        val windChill: GoogleTemperatureValue?,
    @SerializedName("wind")             val wind: GoogleWind?,
    @SerializedName("precipitation")    val precipitation: GooglePrecipitation?,
    @SerializedName("thunderstormProbability") val thunderstormProb: Int?,
    @SerializedName("humidity")         val humidity: Int?,
    @SerializedName("uvIndex")          val uvIndex: Int?,
    @SerializedName("visibility")       val visibility: GoogleVisibility?,
    @SerializedName("cloudCover")       val cloudCover: Int?,
    @SerializedName("pressure")         val pressure: GooglePressure?
)

data class GoogleHourlyResponse(
    @SerializedName("forecastHours") val hours: List<GoogleHourlyItem>?
)

data class GoogleHourlyItem(
    @SerializedName("interval")         val interval: GoogleTimeInterval?,
    @SerializedName("isDaytime")        val isDaytime: Boolean,
    @SerializedName("weatherCondition") val weatherCondition: GoogleWeatherCondition?,
    @SerializedName("temperature")      val temperature: GoogleTemperatureValue?,
    @SerializedName("feelsLikeTemperature") val feelsLike: GoogleTemperatureValue?,
    @SerializedName("wind")             val wind: GoogleWind?,
    @SerializedName("precipitation")    val precipitation: GooglePrecipitation?,
    @SerializedName("thunderstormProbability") val thunderstormProb: Int?,
    @SerializedName("humidity")         val humidity: Int?,
    @SerializedName("uvIndex")          val uvIndex: Int?,
    @SerializedName("cloudCover")       val cloudCover: Int?
)

data class GoogleDailyResponse(
    @SerializedName("forecastDays") val days: List<GoogleDailyItem>?
)

data class GoogleDailyItem(
    @SerializedName("interval")          val interval: GoogleTimeInterval?,
    @SerializedName("daytimeWeatherCondition") val dayCondition: GoogleWeatherCondition?,
    @SerializedName("nighttimeWeatherCondition") val nightCondition: GoogleWeatherCondition?,
    @SerializedName("maxTemperature")    val tempMax: GoogleTemperatureValue?,
    @SerializedName("minTemperature")    val tempMin: GoogleTemperatureValue?,
    @SerializedName("feelsLikeMaxTemperature") val feelsLikeMax: GoogleTemperatureValue?,
    @SerializedName("feelsLikeMinTemperature") val feelsLikeMin: GoogleTemperatureValue?,
    @SerializedName("maxWindSpeed")      val windMax: GoogleWind?,
    @SerializedName("precipitation")     val precipitation: GooglePrecipitation?,
    @SerializedName("thunderstormProbability") val thunderstormProb: Int?,
    @SerializedName("sunriseTime")       val sunrise: String?,
    @SerializedName("sunsetTime")        val sunset: String?,
    @SerializedName("moonriseTime")      val moonrise: String?,
    @SerializedName("moonsetTime")       val moonset: String?,
    @SerializedName("uvIndex")           val uvIndex: Int?
)

// ── Tipi comuni ───────────────────────────────────────────────────────────────

data class GoogleWeatherCondition(
    @SerializedName("type")        val type: String?,       // es. "CLEAR"
    @SerializedName("description") val description: String? // es. "Cielo sereno"
)

data class GoogleTemperatureValue(
    @SerializedName("degrees")    val degrees: Double?,
    @SerializedName("unit")       val unit: String?         // "CELSIUS"
)

data class GoogleWind(
    @SerializedName("speed")     val speed: GoogleWindSpeed?,
    @SerializedName("direction") val direction: GoogleWindDirection?
)

data class GoogleWindSpeed(
    @SerializedName("value") val value: Double?,
    @SerializedName("unit")  val unit: String?  // "KILOMETERS_PER_HOUR"
)

data class GoogleWindDirection(
    @SerializedName("degrees") val degrees: Double?,
    @SerializedName("cardinal") val cardinal: String?  // "N", "SW", ecc.
)

data class GooglePrecipitation(
    @SerializedName("probability")    val probability: GoogleProbability?,
    @SerializedName("qpf")            val qpf: GoogleQpf?
)

data class GoogleProbability(
    @SerializedName("percent") val percent: Int?,
    @SerializedName("type")    val type: String?
)

data class GoogleQpf(
    @SerializedName("quantity") val quantity: Double?,
    @SerializedName("unit")     val unit: String?  // "MILLIMETERS"
)

data class GoogleVisibility(
    @SerializedName("distance") val distance: Double?,
    @SerializedName("unit")     val unit: String?
)

data class GooglePressure(
    @SerializedName("meanSeaLevelMillibars") val mslMb: Double?
)

data class GoogleTimeInterval(
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime")   val endTime: String?
)
