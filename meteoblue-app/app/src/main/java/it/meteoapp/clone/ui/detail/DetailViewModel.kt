package it.meteoapp.clone.ui.detail

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.meteoapp.clone.data.model.DailyForecast
import it.meteoapp.clone.data.model.HourlyForecast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class HourlyStep { ONE_HOUR, THREE_HOURS }

data class DetailUiState(
    val locationName: String = "",
    val dailyForecasts: List<DailyForecast> = emptyList(),
    val hourlyForecasts: List<HourlyForecast> = emptyList(),
    val selectedDayIndex: Int = 0,
    val hourlyStep: HourlyStep = HourlyStep.ONE_HOUR
)

@HiltViewModel
class DetailViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    fun init(
        locationName: String,
        daily: List<DailyForecast>,
        hourly: List<HourlyForecast>,
        initialDayIndex: Int
    ) {
        _state.value = DetailUiState(
            locationName     = locationName,
            dailyForecasts   = daily,
            hourlyForecasts  = hourly,
            selectedDayIndex = initialDayIndex
        )
    }

    fun selectDay(index: Int) {
        _state.value = _state.value.copy(selectedDayIndex = index)
    }

    fun toggleHourlyStep() {
        _state.value = _state.value.copy(
            hourlyStep = if (_state.value.hourlyStep == HourlyStep.ONE_HOUR)
                HourlyStep.THREE_HOURS else HourlyStep.ONE_HOUR
        )
    }

    fun hourlyForDay(dayIndex: Int): List<HourlyForecast> {
        val start = dayIndex * 24
        val step = if (_state.value.hourlyStep == HourlyStep.ONE_HOUR) 1 else 3
        return _state.value.hourlyForecasts
            .drop(start)
            .take(24)
            .filterIndexed { i, _ -> i % step == 0 }
    }
}
