package it.meteoapp.clone.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.meteoapp.clone.ui.theme.*

// ── TopBar con SearchBar ──────────────────────────────────────────────────────
@Composable
fun MeteoTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchActive: (Boolean) -> Unit,
    onGpsClick: () -> Unit,
    isSearchActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // SearchBar
        OutlinedTextField(
            value         = query,
            onValueChange = onQueryChange,
            modifier      = Modifier.weight(1f).height(48.dp),
            placeholder   = {
                Text("Cerca luogo...", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            },
            leadingIcon   = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
            },
            trailingIcon  = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange(""); onSearchActive(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancella", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            } else null,
            singleLine    = true,
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = BackgroundCard,
                unfocusedContainerColor = BackgroundCard,
                focusedBorderColor      = AccentBlue,
                unfocusedBorderColor    = BackgroundCard,
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
        )
        // GPS
        IconButton(onClick = onGpsClick, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.MyLocation, contentDescription = "GPS", tint = TextSecondary)
        }
        // Share
        IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Share, contentDescription = "Condividi", tint = TextSecondary)
        }
        // Menu
        IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextSecondary)
        }
    }
}

// ── Tab row luoghi ────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationTabRow(
    locations: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    onTabLongPress: ((Int) -> Unit)? = null   // long-press → rimozione (non su tab 0 GPS)
) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor   = BackgroundMid,
        contentColor     = TextPrimary,
        edgePadding      = 0.dp,
        indicator        = { /* custom indicator */ }
    ) {
        locations.forEachIndexed { i, name ->
            val isSelected = i == selectedIndex
            val canRemove  = i > 0 && onTabLongPress != null  // tab GPS non rimovibile
            Tab(
                selected = isSelected,
                onClick  = { onTabSelected(i) },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) TabSelected else BackgroundMid)
                    .padding(horizontal = 4.dp)
                    .then(
                        if (canRemove)
                            Modifier.combinedClickable(
                                onClick     = { onTabSelected(i) },
                                onLongClick = { onTabLongPress?.invoke(i) }
                            )
                        else Modifier
                    )
            ) {
                Text(
                    text       = name,
                    modifier   = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                    style      = MaterialTheme.typography.labelMedium,
                    color      = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines   = 1
                )
            }
        }
    }
}

// ── Risultato ricerca ─────────────────────────────────────────────────────────
// "region" mostra comune/provincia/regione (dal display_name di Nominatim)
// per distinguere localita' omonime prima che l'utente selezioni.
@Composable
fun SearchResultItem(name: String, region: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
            if (region.isNotEmpty()) {
                Text(region, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
    HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))
}

// ── Icona meteo ───────────────────────────────────────────────────────────────
// Il campo "pictocode" nei dati orari di MeteoBlue usa uno schema numerico
// "dettagliato" con valori che nei test reali arrivano fino a 33 — non la
// tabella "base" (1-17) che circola nella documentazione/community e che
// avevamo usato in precedenza. Non avendo accesso alla tabella ufficiale
// completa (dominio meteoblue.com non raggiungibile da questo ambiente) e
// avendo verificato che fonti terze non ufficiali si contraddicono tra loro,
// determiniamo pioggia/neve/temporale dai valori meteo reali gia' presenti
// nella risposta (precipitazione, probabilita', frazione di neve) invece che
// dal codice numerico — solo la fascia bassa (1-9, cielo sereno/nuvoloso),
// su cui tutte le fonti concordano, viene letta dal pictocode.
@Composable
fun WeatherIcon(
    pictoCode: Int,
    size: Dp,
    isNight: Boolean = false,
    precipitation: Double = 0.0,
    precipProbability: Int = 0,
    snowFraction: Double = 0.0
) {
    val sky = when {
        pictoCode <= 1 -> if (isNight) "🌙" else "☀️"
        pictoCode <= 3 -> if (isNight) "🌙" else "🌤"
        pictoCode <= 6 -> "⛅"
        else           -> if (isNight) "☁️" else "🌥"
    }

    // Precipitazione non trascurabile: quantita' misurabile o probabilita'
    // sufficientemente alta (sotto il 40% mostriamo solo il cielo di base,
    // per non segnalare pioggia/neve per un rischio marginale).
    val isPrecipitating = precipitation >= 0.1 || precipProbability >= 40
    val emoji = if (!isPrecipitating) {
        sky
    } else if (snowFraction >= 0.5) {
        if (precipitation >= 3.0) "❄️" else "🌨"
    } else if (precipitation >= 3.0) {
        "⛈"
    } else {
        "🌧"
    }
    Text(emoji, fontSize = size.value.sp)
}

// ── Loading indicator ─────────────────────────────────────────────────────────
@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AccentBlue)
    }
}

// ── Messaggio errore ──────────────────────────────────────────────────────────
@Composable
fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CloudOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

// ── Mini-mappa precipitazioni 7x7 (rainspot) ─────────────────────────────────
// Riproduce la mini-mappa radar di MeteoBlue: 49 celle (7x7), una cifra 0-9
// per cella che indica l'intensita' di precipitazione locale prevista.
@Composable
fun RainspotGrid(
    rainspot: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    backgroundColor: Color = RadarDark
) {
    val values = remember(rainspot) {
        IntArray(49) { i -> rainspot.getOrNull(i)?.digitToIntOrNull()?.coerceIn(0, 9) ?: 0 }
    }
    Canvas(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
    ) {
        val gridSize = 7
        val cell = this.size.width / gridSize
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val level = values[row * gridSize + col]
                if (level > 0) {
                    drawRect(
                        color = rainIntensityColor(level),
                        topLeft = Offset(col * cell, row * cell),
                        size = Size(cell, cell)
                    )
                }
            }
        }

        // Mirino radar stile MeteoBlue: 3 cerchi concentrici (esterno, medio,
        // piccolo anello centrale) + 4 trattini di centratura N/S/E/O che
        // attraversano il cerchio esterno, come un reticolo di puntamento.
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val maxRadius = this.size.minDimension / 2f
        val ringColor = Color.White.copy(alpha = 0.45f)
        val ringStroke = Stroke(width = 1.dp.toPx())

        val outerRadius = maxRadius * 0.85f
        val midRadius = maxRadius * 0.55f
        val innerRadius = maxRadius * 0.22f
        listOf(outerRadius, midRadius, innerRadius).forEach { r ->
            drawCircle(color = ringColor, radius = r, center = center, style = ringStroke)
        }

        val tickHalfLength = maxRadius * 0.14f
        val tickDirections = listOf(
            Offset(0f, -1f), // Nord
            Offset(1f, 0f),  // Est
            Offset(0f, 1f),  // Sud
            Offset(-1f, 0f)  // Ovest
        )
        tickDirections.forEach { dir ->
            drawLine(
                color = ringColor,
                start = center + dir * (outerRadius - tickHalfLength),
                end = center + dir * (outerRadius + tickHalfLength),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

fun rainIntensityColor(level: Int): Color {
    val t = (level.coerceIn(1, 9) - 1) / 8f
    return lerp(PrecipBlue, PrecipHeavy, t)
}
