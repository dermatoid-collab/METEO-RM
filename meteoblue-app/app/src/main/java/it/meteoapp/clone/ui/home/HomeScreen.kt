package it.meteoapp.clone.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.meteoapp.clone.data.model.DailyForecast
import it.meteoapp.clone.data.model.HourlyForecast
import it.meteoapp.clone.data.repository.ForecastResult
import it.meteoapp.clone.data.repository.windDirectionArrow
import it.meteoapp.clone.ui.components.*
import it.meteoapp.clone.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onDayClick: (ForecastResult, Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val tabLocations by viewModel.tabLocations.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
    var locationToRemove by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundMid, BackgroundDeep)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── TopBar ──────────────────────────────────────────────────────
            item {
                MeteoTopBar(
                    query          = searchQuery,
                    onQueryChange  = viewModel::onSearchQueryChange,
                    onSearchActive = viewModel::setSearchActive,
                    onGpsClick     = viewModel::loadCurrentLocation,
                    isSearchActive = isSearchActive
                )
            }

            // ── Risultati ricerca ───────────────────────────────────────────
            if (isSearchActive && searchResults.isNotEmpty()) {
                items(searchResults) { loc ->
                    SearchResultItem(
                        name    = loc.name,
                        region  = loc.region,
                        onClick = { viewModel.selectLocation(loc) }
                    )
                }
                return@LazyColumn
            }

            // ── Tab luoghi (GPS fisso + salvati) ────────────────────────────
            if (tabLocations.isNotEmpty()) {
                item {
                    LocationTabRow(
                        locations     = tabLocations.map { it.name },
                        selectedIndex = selectedTab,
                        onTabSelected = viewModel::selectTab,
                        onTabLongPress = { idx -> locationToRemove = idx }
                    )
                }
            }

            // ── Contenuto principale ────────────────────────────────────────
            when (val state = uiState) {
                is HomeUiState.Loading -> item { LoadingIndicator() }
                is HomeUiState.Error   -> item { ErrorMessage(state.message) }
                is HomeUiState.Success -> {
                    val result = state.result

                    // Hero temperatura attuale
                    item {
                        HeroSection(
                            current = result.current,
                            onMeteogramClick = { /* TODO: naviga a meteogramma */ },
                            onRadarClick     = { /* TODO: naviga a mappe */ }
                        )
                    }

                    // Scroll orario
                    item {
                        HourlyScrollRow(result.hourly.take(24))
                    }

                    // Divisore
                    item {
                        HorizontalDivider(
                            color = TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Lista giornaliera
                    items(result.daily) { day ->
                        DailyForecastRow(
                            day     = day,
                            onClick = {
                                val idx = result.daily.indexOf(day)
                                onDayClick(result, idx)
                            }
                        )
                        HorizontalDivider(
                            color = TextMuted.copy(alpha = 0.15f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // ── Dialog conferma rimozione ───────────────────────────────────
        locationToRemove?.let { idx ->
            val loc = tabLocations.getOrNull(idx)
            if (loc != null) {
                AlertDialog(
                    onDismissRequest = { locationToRemove = null },
                    title = { Text("Rimuovi luogo") },
                    text  = { Text("Rimuovere \"${loc.name}\" dai preferiti?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.removeLocation(loc)
                            locationToRemove = null
                        }) { Text("Rimuovi", color = AccentBlue) }
                    },
                    dismissButton = {
                        TextButton(onClick = { locationToRemove = null }) {
                            Text("Annulla", color = TextMuted)
                        }
                    },
                    containerColor = BackgroundCard
                )
            }
        }
    }
}

// ── HeroSection ───────────────────────────────────────────────────────────────
@Composable
fun HeroSection(
    current: it.meteoapp.clone.data.model.CurrentConditions,
    onMeteogramClick: () -> Unit,
    onRadarClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text  = "${current.temperature}°",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = current.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(" • UV ", color = TextSecondary, fontSize = 14.sp)
                Text(
                    text  = "${current.uvIndex}",
                    color = UvMid,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Last update: ${current.lastUpdateMinutes} minutes ago",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        // Pulsanti floating destra
        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick            = onRadarClick,
                containerColor     = BackgroundCard,
                contentColor       = TextPrimary,
                modifier           = Modifier.size(44.dp),
                shape              = RoundedCornerShape(12.dp),
                elevation          = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.GpsFixed, contentDescription = "Radar", modifier = Modifier.size(20.dp))
            }
            FloatingActionButton(
                onClick            = onMeteogramClick,
                containerColor     = BackgroundCard,
                contentColor       = TextPrimary,
                modifier           = Modifier.size(44.dp),
                shape              = RoundedCornerShape(12.dp),
                elevation          = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.BarChart, contentDescription = "Meteogramma", modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ── HourlyScrollRow ───────────────────────────────────────────────────────────
@Composable
fun HourlyScrollRow(hours: List<HourlyForecast>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        hours.forEach { h ->
            HourlyCard(h)
        }
    }
}

@Composable
fun HourlyCard(h: HourlyForecast) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundCard.copy(alpha = 0.6f))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(h.hour, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        WeatherIcon(pictoCode = h.pictoCode, size = 24.dp)
        Text(
            text  = "${h.precipProbability}%",
            style = MaterialTheme.typography.labelSmall,
            color = if (h.precipProbability > 20) PrecipBlue else TextMuted
        )
        Text(
            text  = "${h.temperature}°",
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── DailyForecastRow ──────────────────────────────────────────────────────────
@Composable
fun DailyForecastRow(day: DailyForecast, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Giorno
        Column(modifier = Modifier.width(52.dp)) {
            Text(day.dateLabel, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(day.date, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }

        // Icone giorno + notte + prob precipitazione
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                WeatherIcon(pictoCode = day.pictocodeDay, size = 28.dp)
                WeatherIcon(pictoCode = day.pictocodeNight, size = 22.dp, isNight = true)
            }
            if (day.precipProbability > 5) {
                Text(
                    "${day.precipProbability}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrecipBlue
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Min / Max con box colorati
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            TempBox(temp = day.tempMax, isMax = true)
            TempBox(temp = day.tempMin, isMax = false)
        }

        // Separatore multimodel (4 icone G) — placeholder
        Column(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .size(width = 10.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AccentBlue.copy(alpha = 0.4f))
                )
            }
        }

        // Vento + precipitazione + sole
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "${windDirectionArrow(day.windDirection)} ${day.windSpeedMax.toInt()} km/h",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.End
            )
            val precip = if (day.precipitation > 0) "${day.precipitation} mm" else "–"
            Text(
                "💧 $precip",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.End
            )
            Text(
                "☀ ${day.sunshineHours.toInt()} h",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun TempBox(temp: Int, isMax: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isMax) TempHot else TempMild)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = "$temp°",
            style = MaterialTheme.typography.labelMedium,
            color = BackgroundDeep,
            fontWeight = FontWeight.Bold
        )
    }
}
