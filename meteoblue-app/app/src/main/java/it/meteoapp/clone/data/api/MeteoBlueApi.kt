package it.meteoapp.clone.data.api

import it.meteoapp.clone.data.model.MeteoBlueResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MeteoBlueApi {

    /**
     * Package API — basic-1h
     * Restituisce previsioni orarie (data_1h) e giornaliere (data_day) in un'unica chiamata.
     *
     * DEMOKEY funziona con lat=47.558&lon=7.573 (Basilea) per test.
     * Con API key reale: qualsiasi coordinata.
     */
    @GET("packages/basic-1h_basic-day")
    suspend fun getForecast(
        @Query("lat")     latitude: Double,
        @Query("lon")     longitude: Double,
        @Query("asl")     altitudeMeters: Int = 0,
        @Query("format")  format: String = "json",
        @Query("apikey")  apiKey: String,
        @Query("forecast_days") forecastDays: Int = 7,
        @Query("windspeed") windspeedUnit: String = "kmh",
        @Query("temperature") temperatureUnit: String = "C"
    ): MeteoBlueResponse

    /**
     * Geocoding — cerca luoghi per nome
     * Nota: MeteoBlue non ha un geocoding pubblico diretto,
     * si usa Nominatim (OpenStreetMap) — vedi LocationRepository.
     */
}
