package it.meteoapp.clone.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApi {

    @GET("search")
    suspend fun search(
        @Query("q")              query: String,
        @Query("format")         format: String = "json",
        @Query("limit")          limit: Int = 5,
        @Query("accept-language") lang: String = "it,en",
        // Senza questo parametro Nominatim NON include il campo "address"
        // nella risposta di /search: senza, address risultava sempre null.
        @Query("addressdetails") addressDetails: Int = 1
    ): List<NominatimResult>
}

data class NominatimResult(
    @SerializedName("place_id")    val placeId: Long,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("lat")         val lat: String,
    @SerializedName("lon")         val lon: String,
    @SerializedName("address")     val address: NominatimAddress?
)

data class NominatimAddress(
    @SerializedName("city")        val city: String?,
    @SerializedName("town")        val town: String?,
    @SerializedName("village")     val village: String?,
    @SerializedName("country")     val country: String?
) {
    val locality: String get() = city ?: town ?: village ?: ""
}
