package it.meteoapp.clone.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.meteoapp.clone.core.location.LocationRepository
import it.meteoapp.clone.data.model.LocationSuggestion
import it.meteoapp.clone.data.repository.ForecastRepository
import it.meteoapp.clone.data.repository.ForecastResult
import it.meteoapp.clone.data.repository.SavedLocationsRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val result: ForecastResult) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

// Voce GPS fissa — sempre prima tab
private val GPS_LOCATION = LocationSuggestion(
    name      = "📍 Posizione attuale",
    latitude  = 0.0,
    longitude = 0.0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val forecastRepo: ForecastRepository,
    private val locationRepo: LocationRepository,
    private val savedRepo: SavedLocationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<LocationSuggestion>>(emptyList())
    val searchResults: StateFlow<List<LocationSuggestion>> = _searchResults.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    // Lista tab = GPS fisso + luoghi salvati
    val tabLocations: StateFlow<List<LocationSuggestion>> = savedRepo.savedLocations
        .map { saved -> listOf(GPS_LOCATION) + saved }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(GPS_LOCATION))

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    // Tiene traccia della location correntemente caricata per sincronizzare la tab
    private val _currentLocation = MutableStateFlow<LocationSuggestion?>(null)

    init {
        loadCurrentLocation()
        observeSearch()
        // Sincronizza selectedTabIndex quando la lista cambia (es. dopo add/remove)
        viewModelScope.launch {
            tabLocations.collectLatest { tabs ->
                val currentLoc = _currentLocation.value ?: return@collectLatest
                val idx = tabs.indexOfFirst {
                    it.latitude == currentLoc.latitude && it.longitude == currentLoc.longitude
                }
                if (idx >= 0 && idx != _selectedTabIndex.value) {
                    _selectedTabIndex.value = idx
                }
            }
        }
    }

    fun loadCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            _selectedTabIndex.value = 0 // GPS è sempre tab 0
            val loc = runCatching { locationRepo.getCurrentLocation() }.getOrNull()
            if (loc != null) {
                val gpsEntry = GPS_LOCATION.copy(latitude = loc.lat, longitude = loc.lon)
                _currentLocation.value = gpsEntry
                loadForecast(loc.lat, loc.lon, "La mia posizione")
            } else {
                // Fallback Parma senza permesso GPS
                loadForecast(44.8015, 10.3279, "Parma")
            }
        }
    }

    fun loadForecast(lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            runCatching {
                forecastRepo.getForecast(lat, lon, name)
            }.onSuccess {
                _uiState.value = HomeUiState.Success(it)
            }.onFailure {
                _uiState.value = HomeUiState.Error(
                    it.message ?: "Errore nel caricamento previsioni"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        // Svuota subito i risultati della query precedente: senza, durante il
        // debounce (400ms) restava visibile la lista "vecchia" e un tap
        // rapido poteva selezionare un risultato di un testo piu' corto
        // gia' digitato (es. risultati per "Pa" ancora a schermo mentre si
        // sta ancora scrivendo "Pannocchia").
        _searchResults.value = emptyList()
        if (query.isNotEmpty()) _isSearchActive.value = true
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            _searchResults.value = emptyList()
        }
    }

    fun selectLocation(location: LocationSuggestion) {
        viewModelScope.launch {
            savedRepo.addLocation(location)
            _currentLocation.value = location
            // La tab verrà sincronizzata automaticamente da tabLocations.collectLatest
            loadForecast(location.latitude, location.longitude, location.name)
            setSearchActive(false)
        }
    }

    fun removeLocation(location: LocationSuggestion) {
        viewModelScope.launch {
            savedRepo.removeLocation(location)
            // Se la tab rimossa era quella selezionata → torna a GPS (tab 0)
            val tabs = tabLocations.value
            val removedIdx = tabs.indexOfFirst {
                it.latitude == location.latitude && it.longitude == location.longitude
            }
            if (removedIdx == _selectedTabIndex.value) {
                selectTab(0)
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
        val tabs = tabLocations.value
        when {
            index == 0 -> loadCurrentLocation()  // GPS
            index < tabs.size -> {
                val loc = tabs[index]
                _currentLocation.value = loc
                loadForecast(loc.latitude, loc.longitude, loc.name)
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .filter { it.length >= 2 }
                .collectLatest { query ->
                    runCatching {
                        forecastRepo.searchLocation(query)
                    }.onSuccess {
                        _searchResults.value = it
                    }
                }
        }
    }
}
