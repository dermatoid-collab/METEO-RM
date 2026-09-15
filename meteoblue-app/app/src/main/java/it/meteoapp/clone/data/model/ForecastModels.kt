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
    @SerializedName("windspeed")      val wind: String,         // "km/h"
    @SerializedName("precipitation")  val precipitation: String // "mm"
)

// ── Dati orari ────────────────────────────────────────────────────────────────
data class HourlyData(
    @SerializedName("time")                val time: List<String>,
    @SerializedName("temperature")         val temperature: List<Double>,
    @SerializedName("felttemperature")     val feltTemperature: List<Double>,
    @SerializedName("precipitation")       val precipitation: List<Double>,
    @SerializedName("precipitation_probability") val precipProbability: List<Int>,
    // Nota: il pacchetto basic-1h di MeteoBlue restituisce un solo valore di
    // vento per ora (non un range min-max come nei dati giornalieri) — non
    // esiste alcuna chiave "windspeedmin" in data_1h.
    @SerializedName("windspeed")           val windSpeed: List<Double>,
    @SerializedName("winddirection")       val windDirection: List<Int>,
    @SerializedName("pictocode")           val pictoCode: List<Int>,
    @SerializedName("uvindex")             val uvIndex: List<Int>,
    @SerializedName("relativehumidity")    val humidity: List<Int>,
    // Mini-mappa precipitazioni 7x7 (49 caratteri, cifre 0-9 = intensita' per cella)
    @SerializedName("rainspot")            val rainspot: List<String>? = null,
    // Frazione (0-1) della precipitazione che cade come neve: usata per
    // decidere pioggia/neve al posto del solo pictocode (vedi nota in
    // ForecastRepository sulla tabella pictocode non verificabile).
    @SerializedName("snowfraction")        val snowFraction: List<Double>? = null
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
    // Il pacchetto basic-day restituisce un solo pictocode giornaliero: non
    // esiste uno split giorno/notte senza il pacchetto MeteoBlue "sunmoon".
    @SerializedName("pictocode")            val pictoCode: List<Int>,
    @SerializedName("uvindex")              val uvIndex: List<Int>,
    @SerializedName("sealevelpressure_max") val pressureMax: List<Double>,
    // Campi disponibili solo con il pacchetto MeteoBlue "sunmoon" (non
    // incluso nel piano basic-1h_basic-day) — assenti dalla risposta reale,
    // quindi opzionali per non far fallire il parsing.
    @SerializedName("sunshine_time")        val sunshineHours: List<Double>? = null,
    @SerializedName("sunrise")              val sunrise: List<String>? = null,
    @SerializedName("sunset")               val sunset: List<String>? = null,
    @SerializedName("moonrise")             val moonrise: List<String>? = null,
    @SerializedName("moonset")              val moonset: List<String>? = null,
    // Mini-mappa precipitazioni 7x7 (49 caratteri, cifre 0-9 = intensita' per cella)
    @SerializedName("rainspot")             val rainspot: List<String>? = null,
    @SerializedName("snowfraction")         val snowFraction: List<Double>? = null
)

// ── Mini-mappa precipitazioni 7x7 ────────────────────────────────────────────
// Stringa di 49 cifre (0-9, riga per riga) come restituita da MeteoBlue in
// "rainspot": nessun dato equivalente da altre fonti (es. Google Weather),
// quindi il default e' una griglia vuota (nessuna precipitazione).
const val EMPTY_RAINSPOT = "0000000000000000000000000000000000000000000000000"

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
    val windSpeed: String,    // "22" (valore singolo, il pacchetto orario non fornisce un range)
    val windDirection: Int,
    val pictoCode: Int,
    val uvIndex: Int,
    val humidity: Int,
    val rainspot: String = EMPTY_RAINSPOT,
    val snowFraction: Double = 0.0
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
    val humidity: Int,        // dal primo dato orario del giorno
    val rainspot: String = EMPTY_RAINSPOT,
    val snowFraction: Double = 0.0
)

data class LocationSuggestion(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String = "",
    // Comune/provincia/regione per distinguere localita' omonime nei
    // risultati di ricerca (es. "Collecchio, Parma, Emilia-Romagna").
    val region: String = ""
)
