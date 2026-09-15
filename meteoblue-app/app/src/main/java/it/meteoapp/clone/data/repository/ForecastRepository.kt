package it.meteoapp.clone.data.repository

import it.meteoapp.clone.data.api.MeteoBlueApi
import it.meteoapp.clone.data.api.NominatimApi
import it.meteoapp.clone.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ForecastRepository @Inject constructor(
    private val api: MeteoBlueApi,
    private val nominatimApi: NominatimApi,
    @Named("meteoblue") private val apiKey: String  // iniettato come String
) {
    // ── Fetch previsioni ──────────────────────────────────────────────────────
    suspend fun getForecast(
        lat: Double,
        lon: Double,
        locationName: String
    ): ForecastResult {
        val response = api.getForecast(
            latitude  = lat,
            longitude = lon,
            apiKey    = apiKey
        )

        // MeteoBlue restituisce HTTP 200 anche per errori applicativi (chiave/pacchetto
        // non validi, quota esaurita, ecc.); in quel caso i campi dati risultano nulli
        // nonostante il tipo Kotlin non-null (limite noto di Gson + reflection).
        @Suppress("SENSELESS_COMPARISON")
        val isIncomplete = response.hourly == null || response.daily == null ||
            response.hourly.time == null || response.daily.time == null

        if (response.errorMessage != null || isIncomplete) {
            throw IllegalStateException(
                response.errorMessage
                    ?: "Dati meteo non disponibili per questa posizione. Verifica che la tua chiave API MeteoBlue includa i pacchetti \"basic-1h\" e \"basic-day\"."
            )
        }

        return mapToForecastResult(response, locationName)
    }

    // ── Geocoding ─────────────────────────────────────────────────────────────
    suspend fun searchLocation(query: String): List<LocationSuggestion> {
        return nominatimApi.search(query).map { result ->
            val shortName = result.address?.locality
                ?.takeIf { it.isNotEmpty() }
                ?: result.displayName.split(",").firstOrNull()?.trim()
                ?: result.displayName
            // Il resto della gerarchia (comune, provincia, regione...) dopo il
            // primo componente del display_name di Nominatim: permette di
            // distinguere localita' omonime (es. due "Pannocchia" in regioni
            // diverse) gia' nella lista dei risultati, prima di selezionare.
            val region = result.displayName.substringAfter(",", "").trim()
            LocationSuggestion(
                name      = shortName,
                latitude  = result.lat.toDouble(),
                longitude = result.lon.toDouble(),
                country   = result.address?.country ?: "",
                region    = region
            )
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────
    private fun mapToForecastResult(
        r: MeteoBlueResponse,
        locationName: String
    ): ForecastResult {
        val h = r.hourly
        val d = r.daily

        // Ora corrente → indice orario
        val now = LocalDateTime.now()
        val currentHourIdx = h.time.indexOfFirst { timeStr ->
            runCatching {
                LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    .hour == now.hour
            }.getOrDefault(false)
        }.coerceAtLeast(0)

        val current = CurrentConditions(
            locationName    = locationName,
            temperature     = h.temperature[currentHourIdx].toInt(),
            description     = pictoCodeToDescription(h.pictoCode[currentHourIdx]),
            uvIndex         = h.uvIndex[currentHourIdx],
            lastUpdateMinutes = 0,
            pictoCode       = h.pictoCode[currentHourIdx]
        )

        val hourlyForecasts = h.time.mapIndexed { i, time ->
            val hour = runCatching {
                LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).hour
            }.getOrDefault(i % 24)
            HourlyForecast(
                hour             = "%02d".format(hour),
                temperature      = h.temperature[i].toInt(),
                feltTemperature  = h.feltTemperature.getOrElse(i) { h.temperature[i] }.toInt(),
                precipProbability = h.precipProbability.getOrElse(i) { 0 },
                precipitation    = h.precipitation.getOrElse(i) { 0.0 },
                windSpeed        = "${h.windSpeed.getOrElse(i) { 0.0 }.toInt()}",
                windDirection    = h.windDirection.getOrElse(i) { 0 },
                pictoCode        = h.pictoCode[i],
                uvIndex          = h.uvIndex.getOrElse(i) { 0 },
                humidity         = h.humidity.getOrElse(i) { 0 },
                rainspot         = h.rainspot?.getOrElse(i) { EMPTY_RAINSPOT } ?: EMPTY_RAINSPOT
            )
        }

        val dailyForecasts = d.time.mapIndexed { i, dateStr ->
            val date = runCatching {
                LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }.getOrNull()
            val dow = date?.dayOfWeek?.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) ?: ""
            val dateLabel = date?.let { "${it.monthValue}/${it.dayOfMonth}" } ?: ""

            // Umidità media del giorno (prendi dalle ore del giorno)
            val dayStart = i * 24
            val dayHumidity = (dayStart until (dayStart + 24).coerceAtMost(h.humidity.size))
                .mapNotNull { h.humidity.getOrNull(it) }
                .average().toInt()

            DailyForecast(
                dateLabel       = dow,
                dayOfWeek       = date?.dayOfWeek?.getDisplayName(TextStyle.FULL, Locale.ENGLISH) ?: "",
                date            = dateLabel,
                tempMax         = d.tempMax[i].toInt(),
                tempMin         = d.tempMin[i].toInt(),
                precipProbability = d.precipProbability.getOrElse(i) { 0 },
                precipitation   = d.precipitation.getOrElse(i) { 0.0 },
                windSpeedMax    = d.windSpeedMax.getOrElse(i) { 0.0 },
                windSpeedMin    = d.windSpeedMin.getOrElse(i) { 0.0 },
                windDirection   = d.winddirection.getOrElse(i) { 0 },
                // Nessuno split giorno/notte disponibile nel piano attivo: si
                // riusa lo stesso pictocode per entrambe le icone.
                pictocodeDay    = d.pictoCode.getOrElse(i) { 1 },
                pictocodeNight  = d.pictoCode.getOrElse(i) { 1 },
                uvIndex         = d.uvIndex.getOrElse(i) { 0 },
                // Alba/tramonto/fasi lunari richiedono il pacchetto MeteoBlue
                // "sunmoon" (non attivo): restituiamo un placeholder se assenti.
                sunshineHours   = d.sunshineHours?.getOrElse(i) { 0.0 } ?: 0.0,
                sunrise         = d.sunrise?.getOrElse(i) { "--:--" }?.takeLast(5) ?: "--:--",
                sunset          = d.sunset?.getOrElse(i) { "--:--" }?.takeLast(5) ?: "--:--",
                moonrise        = d.moonrise?.getOrElse(i) { "--:--" }?.takeLast(5) ?: "--:--",
                moonset         = d.moonset?.getOrElse(i) { "--:--" }?.takeLast(5) ?: "--:--",
                pressureMax     = d.pressureMax.getOrElse(i) { 1013.0 },
                humidity        = dayHumidity,
                rainspot        = d.rainspot?.getOrElse(i) { EMPTY_RAINSPOT } ?: EMPTY_RAINSPOT
            )
        }

        return ForecastResult(
            current  = current,
            hourly   = hourlyForecasts,
            daily    = dailyForecasts,
            metadata = r.metadata
        )
    }

    // ── Estensione mancante nel model ──────────────────────────────────────────
    private val DailyData.winddirection: List<Int>
        get() = windDirection
}

data class ForecastResult(
    val current: CurrentConditions,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val metadata: Metadata
)

// ── Mappa pictocode MeteoBlue → descrizione testuale ─────────────────────────
fun pictoCodeToDescription(code: Int): String = when (code) {
    1    -> "Clear, cloudless sky"
    2    -> "Clear, few cirrus clouds"
    3    -> "Clear with cirrus clouds"
    4    -> "Clear with few low clouds"
    5    -> "Clear with few low clouds and cirrus"
    6    -> "Clear with low clouds"
    7    -> "Partly cloudy"
    8    -> "Partly cloudy and few low clouds"
    9    -> "Partly cloudy"
    10   -> "Mixed with some thunderstorm clouds"
    11   -> "Mixed with few showers"
    12   -> "Mixed with showers"
    13   -> "Overcast with rain"
    14   -> "Overcast with snow"
    15   -> "Overcast with heavy rain"
    16   -> "Overcast with heavy snow"
    17   -> "Mixed with sleet"
    18   -> "Overcast with sleet"
    19   -> "Overcast with freezing rain"
    20   -> "Thunderstorms, hail possible"
    21   -> "Mostly overcast"
    22   -> "Overcast"
    23   -> "Overcast with light rain"
    24   -> "Overcast with light snow"
    25   -> "Overcast with heavy rain and thunder"
    26   -> "Partly cloudy, showers likely"
    27   -> "Showers, thunderstorms likely"
    else -> "Variable conditions"
}

// ── Direzione vento → simbolo freccia ────────────────────────────────────────
fun windDirectionArrow(degrees: Int): String = when ((degrees / 45) % 8) {
    0    -> "↓"  // N→S (vento da N, freccia verso S)
    1    -> "↙"
    2    -> "←"
    3    -> "↖"
    4    -> "↑"
    5    -> "↗"
    6    -> "→"
    7    -> "↘"
    else -> "→"
}
