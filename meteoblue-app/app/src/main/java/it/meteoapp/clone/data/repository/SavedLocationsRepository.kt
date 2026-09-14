package it.meteoapp.clone.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import it.meteoapp.clone.data.model.LocationSuggestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "saved_locations")

@Singleton
class SavedLocationsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val KEY = stringPreferencesKey("locations_json")

    val savedLocations: Flow<List<LocationSuggestion>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY] ?: return@map emptyList()
        val type = object : TypeToken<List<LocationSuggestion>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    suspend fun addLocation(location: LocationSuggestion) {
        context.dataStore.edit { prefs ->
            val current = run {
                val json = prefs[KEY] ?: "[]"
                val type = object : TypeToken<List<LocationSuggestion>>() {}.type
                gson.fromJson<List<LocationSuggestion>>(json, type)?.toMutableList()
                    ?: mutableListOf()
            }
            if (current.none { it.latitude == location.latitude && it.longitude == location.longitude }) {
                current.add(0, location)
            }
            prefs[KEY] = gson.toJson(current.take(10)) // max 10 luoghi
        }
    }

    suspend fun removeLocation(location: LocationSuggestion) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY] ?: return@edit
            val type = object : TypeToken<List<LocationSuggestion>>() {}.type
            val current = gson.fromJson<List<LocationSuggestion>>(json, type)?.toMutableList()
                ?: return@edit
            current.removeAll { it.latitude == location.latitude && it.longitude == location.longitude }
            prefs[KEY] = gson.toJson(current)
        }
    }
}
