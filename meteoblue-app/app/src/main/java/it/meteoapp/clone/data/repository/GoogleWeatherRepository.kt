package it.meteoapp.clone.data.repository

import it.meteoapp.clone.data.api.GoogleWeatherApi
import it.meteoapp.clone.data.model.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GoogleWeatherRepository @Inject constructor(
    private val api: GoogleWeatherApi,
    @Named("google") private val apiKey: String
) {
    suspend fun getForecast(
        lat: Double,
        lon: Double,
        locationName: String
    ): ForecastResult {
        // Tre chiamate parallele — in produzione usare async/await
        val currentResp = api.getCurrentConditions(lat, lon, apiKey)
        val hourlyResp  = api.getHourlyForecast(lat, lon, apiKey, hours = 168) // 7 giorni
        val dailyResp   = api.getDailyForecast(lat, lon, apiKey, days = 7)

        return mapToForecastResult(currentResp, hourlyResp, dailyResp, locationName, lat, lon)
    }

    private fun mapToForecastResult(
        currentResp: it.meteoapp.clone.data.api.GoogleCurrentResponse,
        hourlyResp:  it.meteoapp.clone.data.api.GoogleHourlyResponse,
        dailyResp:   it.meteoapp.clone.data.api.GoogleDailyResponse,
        locationName: String,
        lat: Double,
        lon: Double
    ): ForecastResult {

        val cond = currentResp.conditions

        val current = CurrentConditions(
            locationName      = locationName,
            temperature       = cond?.temperature?.degrees?.toInt() ?: 0,
            description       = cond?.weatherCondition?.description ?: "–",
            uvIndex           = cond?.uvIndex ?: 0,
            lastUpdateMinutes = 0,
            pictoCode         = googleTypeToPickoCode(cond?.weatherCondition?.type)
        )

        val hourly = hourlyResp.hours?.mapIndexed { i, h ->
            val hour = h.interval?.startTime?.let { parseHour(it) } ?: i % 24
            HourlyForecast(
                hour              = "%02d".format(hour),
                temperature       = h.temperature?.degrees?.toInt() ?: 0,
                feltTemperature   = h.feelsLike?.degrees?.toInt() ?: 0,
                precipProbability = h.precipitation?.probability?.percent ?: 0,
                precipitation     = h.precipitation?.qpf?.quantity ?: 0.0,
                windSpeed         = windSpeedRange(h.wind?.speed?.value),
                windDirection     = h.wind?.direction?.degrees?.toInt() ?: 0,
                pictoCode         = googleTypeToPickoCode(h.weatherCondition?.type),
                uvIndex           = h.uvIndex ?: 0,
                humidity          = h.humidity ?: 0
            )
        } ?: emptyList()

        val daily = dailyResp.days?.mapIndexed { i, d ->
            val date = d.interval?.startTime?.let { parseDate(it) }
            val dow = date?.dayOfWeek?.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) ?: ""
            val dateLabel = date?.let { "${it.monthValue}/${it.dayOfMonth}" } ?: ""
            val dayHumidity = hourly.drop(i * 24).take(24).map { it.humidity }.average()
                .let { if (it.isNaN()) 0 else it.toInt() }

            DailyForecast(
                dateLabel         = dow,
                dayOfWeek         = date?.dayOfWeek?.getDisplayName(TextStyle.FULL, Locale.ENGLISH) ?: "",
                date              = dateLabel,
                tempMax           = d.tempMax?.degrees?.toInt() ?: 0,
                tempMin           = d.tempMin?.degrees?.toInt() ?: 0,
                precipProbability = d.precipitation?.probability?.percent ?: 0,
                precipitation     = d.precipitation?.qpf?.quantity ?: 0.0,
                windSpeedMax      = d.windMax?.speed?.value ?: 0.0,
                windSpeedMin      = (d.windMax?.speed?.value ?: 0.0) * 0.6,
                windDirection     = d.windMax?.direction?.degrees?.toInt() ?: 0,
                pictocodeDay      = googleTypeToPickoCode(d.dayCondition?.type),
                pictocodeNight    = googleTypeToPickoCode(d.nightCondition?.type, night = true),
                uvIndex           = d.uvIndex ?: 0,
                sunshineHours     = estimateSunshineHours(d.dayCondition?.type),
                sunrise           = d.sunrise?.let { parseTimeOnly(it) } ?: "--:--",
                sunset            = d.sunset?.let { parseTimeOnly(it) } ?: "--:--",
                moonrise          = d.moonrise?.let { parseTimeOnly(it) } ?: "--:--",
                moonset           = d.moonset?.let { parseTimeOnly(it) } ?: "--:--",
                pressureMax       = 1013.0, // non fornito da Google daily
                humidity          = dayHumidity
            )
        } ?: emptyList()

        return ForecastResult(
            current  = current,
            hourly   = hourly,
            daily    = daily,
            metadata = Metadata(
                name       = locationName,
                latitude   = lat,
                longitude  = lon,
                altitude   = 0,
                lastUpdate = "",
                utcOffset  = 0.0
            )
        )
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private fun parseHour(isoTime: String): Int = runCatching {
        OffsetDateTime.parse(isoTime).hour
    }.getOrDefault(0)

    private fun parseDate(isoTime: String) = runCatching {
        OffsetDateTime.parse(isoTime).toLocalDate()
    }.getOrNull()

    private fun parseTimeOnly(isoTime: String): String = runCatching {
        val t = OffsetDateTime.parse(isoTime)
        "%02d:%02d".format(t.hour, t.minute)
    }.getOrDefault("--:--")

    private fun windSpeedRange(kmh: Double?): String {
        if (kmh == null) return "0"
        val min = (kmh * 0.7).toInt()
        return "$min-${kmh.toInt()}"
    }

    private fun estimateSunshineHours(conditionType: String?): Double = when {
        conditionType == null                  -> 6.0
        conditionType.contains("CLEAR")        -> 11.0
        conditionType.contains("PARTLY")       -> 7.0
        conditionType.contains("CLOUD")        -> 3.0
        conditionType.contains("RAIN")         -> 1.0
        conditionType.contains("STORM")        -> 0.0
        conditionType.contains("SNOW")         -> 2.0
        else                                   -> 5.0
    }

    /**
     * Converte il tipo stringa Google Weather → pictocode MeteoBlue equivalente.
     * Permette di riusare WeatherIcon e la logica esistente senza duplicazioni.
     */
    fun googleTypeToPickoCode(type: String?, night: Boolean = false): Int = when {
        type == null                       -> 1
        type == "CLEAR"                    -> if (night) 1 else 1
        type == "MOSTLY_CLEAR"             -> 2
        type == "PARTLY_CLOUDY"            -> 7
        type == "MOSTLY_CLOUDY"            -> 9
        type == "CLOUDY"                   -> 22
        type == "OVERCAST"                 -> 22
        type.contains("DRIZZLE")           -> 11
        type.contains("LIGHT_RAIN")        -> 12
        type == "RAIN"                     -> 13
        type.contains("HEAVY_RAIN")        -> 15
        type.contains("FREEZING")          -> 19
        type.contains("LIGHT_SNOW")        -> 24
        type == "SNOW"                     -> 14
        type.contains("HEAVY_SNOW")        -> 16
        type.contains("SLEET")             -> 17
        type.contains("THUNDERSTORM")      -> 27
        type.contains("HAIL")              -> 20
        type.contains("FOG")               -> 21
        else                               -> 7
    }
}

// ── Modello confronto ─────────────────────────────────────────────────────────
data class ComparisonForecast(
    val hour: String,
    val meteoblueTemp: Int,
    val googleTemp: Int,
    val meteoBluePrecipProb: Int,
    val googlePrecipProb: Int,
    val meteoblueWindKmh: String,
    val googleWindKmh: String,
    val deltaTemp: Int           // Google - MeteoBlue
)

fun buildComparison(
    meteoblue: List<HourlyForecast>,
    google: List<HourlyForecast>,
    hours: Int = 24
): List<ComparisonForecast> {
    val mbMap = meteoblue.take(hours).associateBy { it.hour }
    val gMap  = google.take(hours).associateBy { it.hour }
    return mbMap.keys.intersect(gMap.keys).sorted().map { hour ->
        val mb = mbMap[hour]!!
        val g  = gMap[hour]!!
        ComparisonForecast(
            hour              = hour,
            meteoblueTemp     = mb.temperature,
            googleTemp        = g.temperature,
            meteoBluePrecipProb = mb.precipProbability,
            googlePrecipProb  = g.precipProbability,
            meteoblueWindKmh  = mb.windSpeed,
            googleWindKmh     = g.windSpeed,
            deltaTemp         = g.temperature - mb.temperature
        )
    }
}
