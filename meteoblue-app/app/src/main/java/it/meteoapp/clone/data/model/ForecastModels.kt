package it.meteoapp.clone.data.model

import com.google.gson.annotations.SerializedName

// ── Risposta root ─────────────────────────────────────────────────────────────
data class MeteoBlueResponse(
    @SerializedName("metadata")       val metadata: Metadata,
    @SerializedName("units")          val units: Units,
    @SerializedName("data_1h")        val hourly: HourlyData,
    @SerializedName("data_day")       val daily: DailyData,
    // Presente solo nelle risposte di errore (es. chiave/pacchetto non validi)
    @SerializedName("error_message")  val errorMessage: String? = null
)

data class Metadata(
    @SerializedName("name")           val name: String,
    @SerializedName("latitude")       val latitude: Double,
    @SerializedName("longitude")      val longitude: Double,
    @SerializedName("height")         val altitude: Int,
    @SerializedName("modelrun_utc")   val lastUpdate: String,
    @SerializedName("utc_timeoffset") val utcOffset: Double
)

data class Units(
    @SerializedName("temperature")    val temperature: String,  // "°C"
    @SerializedName("wind")           val wind: String,         // "km/h"
    @SerializedName("precipitation")  val precipitation: String // "mm"
)

// ── Dati orari ────────────────────────────────────────────────────────────────
data class HourlyData(
    @SerializedName("time")                val time: List<String>,
    @SerializedName("temperature")         val temperature: List<Double>,
    @SerializedName("felttemperature")     val feltTemperature: List<Double>,
    @SerializedName("precipitation")       val precipitation: List<Double>,
    @SerializedName("precipitation_probability") val precipProbability: List<Int>,
    @SerializedName("windspeed")           val windSpeed: List<Double>,
    @SerializedName("windspeedmin")        val windSpeedMin: List<Double>,
    @SerializedName("winddirection")       val windDirection: List<Int>,
    @SerializedName("pictocode")           val pictoCode: List<Int>,
    @SerializedName("uvindex")             val uvIndex: List<Int>,
    @SerializedName("relativehumidity")    val humidity: List<Int>
)

// ── Dati giornalieri ──────────────────────────────────────────────────────────
data class DailyData(
    @SerializedName("time")                 val time: List<String>,
    @SerializedName("temperature_max")      val tempMax: List<Double>,
    @SerializedName("temperature_min")      val tempMin: List<Double>,
    @SerializedName("precipitation")        val precipitation: List<Double>,
    @SerializedName("precipitation_probability") val precipProbability: List<Int>,
    @SerializedName("windspeed_max")        val windSpeedMax: List<Double>,
    @SerializedName("windspeed_min")        val windSpeedMin: List<Double>,
    @SerializedName("winddirection")        val windDirection: List<Int>,
    @SerializedName("pictocode_day")        val pictocodeDay: List<Int>,
    @SerializedName("pictocode_night")      val pictocodeNight: List<Int>,
    @SerializedName("uvindex")              val uvIndex: List<Int>,
    @SerializedName("sunshine_time")        val sunshineHours: List<Double>,
    @SerializedName("sunrise")              val sunrise: List<String>,
    @SerializedName("sunset")              val sunset: List<String>,
    @SerializedName("moonrise")             val moonrise: List<String>,
    @SerializedName("moonset")             val moonset: List<String>,
    @SerializedName("sealevelpressure_max") val pressureMax: List<Double>
)

// ── UI Models (dominio) ───────────────────────────────────────────────────────
data class CurrentConditions(
    val locationName: String,
    val temperature: Int,
    val description: String,
    val uvIndex: Int,
    val lastUpdateMinutes: Int,
    val pictoCode: Int
)

data class HourlyForecast(
    val hour: String,         // "06", "07"...
    val temperature: Int,
    val feltTemperature: Int,
    val precipProbability: Int,
    val precipitation: Double,
    val windSpeed: String,    // "7-22"
    val windSpeedMin: Double,
    val windDirection: Int,
    val pictoCode: Int,
    val uvIndex: Int,
    val humidity: Int
)

data class DailyForecast(
    val dateLabel: String,    // "Mon\n9/14"
    val dayOfWeek: String,    // "Monday"
    val date: String,         // "9/14"
    val tempMax: Int,
    val tempMin: Int,
    val precipProbability: Int,
    val precipitation: Double,
    val windSpeedMax: Double,
    val windSpeedMin: Double,
    val windDirection: Int,
    val pictocodeDay: Int,
    val pictocodeNight: Int,
    val uvIndex: Int,
    val sunshineHours: Double,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    val pressureMax: Double,
    val humidity: Int         // dal primo dato orario del giorno
)

data class LocationSuggestion(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String = ""
)
