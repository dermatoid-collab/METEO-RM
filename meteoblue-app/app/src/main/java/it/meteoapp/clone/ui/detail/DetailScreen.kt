package it.meteoapp.clone.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import it.meteoapp.clone.data.model.DailyForecast
import it.meteoapp.clone.data.model.HourlyForecast
import it.meteoapp.clone.data.repository.ForecastResult
import it.meteoapp.clone.data.repository.weatherDescription
import it.meteoapp.clone.data.repository.windDirectionArrow
import it.meteoapp.clone.ui.components.RainspotGrid
import it.meteoapp.clone.ui.components.WeatherIcon
import it.meteoapp.clone.ui.home.TempBox
import it.meteoapp.clone.ui.theme.*

@Composable
fun DetailScreen(
    forecastResult: ForecastResult,
    initialDayIndex: Int,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    LaunchedEffect(forecastResult) {
        viewModel.init(
            locationName    = forecastResult.current.locationName,
            daily           = forecastResult.daily,
            hourly          = forecastResult.hourly,
            initialDayIndex = initialDayIndex
        )
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedDay = state.dailyForecasts.getOrNull(state.selectedDayIndex) ?: return
    val hourlyForDay = viewModel.hourlyForDay(state.selectedDayIndex)

    Box(modifier = Modifier.fillMaxSize()) {
        // Sfondo fotografico dinamico
        WeatherBackgroundImage(pictoCode = selectedDay.pictocodeDay)

        // Overlay scuro per leggibilità
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            BackgroundDeep.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // TopBar
            item {
                DetailTopBar(
                    locationName = state.locationName,
                    onBack       = onBack
                )
            }

            // Pager dots (7 giorni)
            item {
                DayPagerDots(
                    count         = state.dailyForecasts.size,
                    selectedIndex = state.selectedDayIndex,
                    onSelect      = viewModel::selectDay
                )
            }

            // Hero del giorno
            item {
                DayHeroSection(day = selectedDay)
            }

            // Info box: alba/tramonto, luna, pressione, umidità
            item {
                DayInfoBox(day = selectedDay)
            }

            // Header hourly forecast con toggle
            item {
                HourlyForecastHeader(
                    step     = state.hourlyStep,
                    onToggle = viewModel::toggleHourlyStep
                )
            }

            // Lista oraria
            items(hourlyForDay) { h ->
                HourlyForecastRow(h)
                HorizontalDivider(
                    color = TextMuted.copy(alpha = 0.15f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// ── Sfondo fotografico ────────────────────────────────────────────────────────
@Composable
fun WeatherBackgroundImage(pictoCode: Int) {
    val imageUrl = pictoCodeToBackgroundUrl(pictoCode)
    AsyncImage(
        model              = imageUrl,
        contentDescription = null,
        contentScale       = ContentScale.Crop,
        modifier           = Modifier.fillMaxWidth().height(320.dp)
    )
}

fun pictoCodeToBackgroundUrl(code: Int): String {
    // Usa Unsplash source per categorie meteo — sostituibile con asset locali
    return when {
        code in 1..3   -> "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?w=800"  // cielo limpido
        code in 4..9   -> "https://images.unsplash.com/photo-1534088568595-a066f410bcda?w=800"  // nuvoloso
        code in 10..13 -> "https://images.unsplash.com/photo-1519692933481-e162a57d6721?w=800"  // pioggia
        code in 14..18 -> "https://images.unsplash.com/photo-1548777123-e216912df7d8?w=800"     // neve
        code >= 19     -> "https://images.unsplash.com/photo-1472145246862-b24cf25495bf?w=800"  // temporale
        else           -> "https://images.unsplash.com/photo-1534088568595-a066f410bcda?w=800"
    }
}

// ── TopBar ────────────────────────────────────────────────────────────────────
@Composable
fun DetailTopBar(locationName: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = TextPrimary)
        }
        Text(
            text     = locationName,
            style    = MaterialTheme.typography.titleLarge,
            color    = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { }) {
            Icon(Icons.Default.Share, contentDescription = "Condividi", tint = TextPrimary)
        }
    }
}

// ── Pager dots ────────────────────────────────────────────────────────────────
@Composable
fun DayPagerDots(count: Int, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            val isSelected = i == selectedIndex
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isSelected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) TextPrimary else TextMuted.copy(alpha = 0.5f))
                    .clickable { onSelect(i) }
            )
        }
    }
}

// ── Hero del giorno ───────────────────────────────────────────────────────────
@Composable
fun DayHeroSection(day: DailyForecast) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text  = "${day.date}",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Text(
            text  = day.dayOfWeek,
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                weatherDescription(
                    pictoCode         = day.pictocodeDay,
                    precipitation     = day.precipitation,
                    precipProbability = day.precipProbability,
                    snowFraction      = day.snowFraction
                ),
                color = TextSecondary, fontSize = 14.sp
            )
            Text(" • UV ", color = TextSecondary, fontSize = 14.sp)
            Text("${day.uvIndex}", color = UvMid, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

// ── Info box: alba/tramonto ecc. ──────────────────────────────────────────────
@Composable
fun DayInfoBox(day: DailyForecast) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundCard.copy(alpha = 0.75f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Alba / tramonto
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(emoji = "🌅", label = "↑ ${day.sunrise}", sub = "↓ ${day.sunset}")
            }
            // Luna
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(emoji = "🌙", label = "↑ ${day.moonrise}", sub = "↓ ${day.moonset}")
            }
            // Pressione + umidità
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⬇ ", color = TextSecondary, fontSize = 12.sp)
                    Text("${day.pressureMax.toInt()} hPa", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💧 ", color = TextSecondary, fontSize = 12.sp)
                    Text("${day.humidity} %", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun InfoRow(emoji: String, label: String, sub: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Text(sub, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 20.dp))
    }
}

// ── Hourly forecast header ────────────────────────────────────────────────────
@Composable
fun HourlyForecastHeader(step: HourlyStep, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "HOURLY FORECAST",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(BackgroundCard)
                .clickable(onClick = onToggle)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text  = if (step == HourlyStep.ONE_HOUR) "1h" else "3h",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary
            )
        }
    }
}

// ── Riga oraria ───────────────────────────────────────────────────────────────
@Composable
fun HourlyForecastRow(h: HourlyForecast) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ora + temperatura con box colorato
        Box(
            modifier = Modifier
                .width(60.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundCard)
                        .padding(vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(h.hour + ":00", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(temperatureColor(h.temperature))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${h.temperature}°",
                        style = MaterialTheme.typography.labelMedium,
                        color = BackgroundDeep,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // Icona meteo
        WeatherIcon(
            pictoCode         = h.pictoCode,
            size              = 32.dp,
            precipitation     = h.precipitation,
            precipProbability = h.precipProbability,
            snowFraction      = h.snowFraction
        )

        Spacer(Modifier.width(8.dp))

        // Indice comfort (mano) + temperatura percepita
        Column(
            modifier = Modifier.width(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🖐", fontSize = 16.sp)
            Text("${h.feltTemperature}°", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }

        Spacer(Modifier.weight(1f))

        // Dati meteo: vento, precipitazione, %
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${windDirectionArrow(h.windDirection)} ${h.windSpeed} km/h",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            val precipText = if (h.precipitation < 1.0) "< 1 mm" else "${h.precipitation} mm"
            Text("💧 $precipText", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text("💧 ${h.precipProbability} %", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }

        Spacer(Modifier.width(8.dp))

        // Mini-mappa precipitazioni 7x7 (rainspot), stile MeteoBlue
        RainspotGrid(rainspot = h.rainspot)
    }
}

// ── Utility: colore da temperatura ───────────────────────────────────────────
fun temperatureColor(temp: Int): Color = when {
    temp >= 28 -> TempHot
    temp >= 18 -> Color(0xFFADD06A)  // verde giallino
    temp >= 10 -> TempMild
    else       -> TempCold
}
