package it.meteoapp.clone.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
            onValueChange  // bug: duplicated, handled below
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
@Composable
fun SearchResultItem(name: String, country: String, onClick: () -> Unit) {
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
            if (country.isNotEmpty()) {
                Text(country, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
    HorizontalDivider(color = TextMuted.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))
}

// ── Icona meteo da pictocode ──────────────────────────────────────────────────
@Composable
fun WeatherIcon(pictoCode: Int, size: Dp, isNight: Boolean = false) {
    // Emoji mapping — in produzione sostituire con icone SVG MeteoBlue
    val emoji = when {
        isNight && pictoCode <= 3 -> "🌙"
        isNight && pictoCode <= 9 -> "🌥"
        pictoCode == 1            -> "☀️"
        pictoCode in 2..3         -> "🌤"
        pictoCode in 4..6         -> "⛅"
        pictoCode in 7..9         -> "🌥"
        pictoCode in 10..12       -> "🌦"
        pictoCode in 13..13       -> "🌧"
        pictoCode in 14..16       -> "❄️"
        pictoCode in 17..18       -> "🌨"
        pictoCode >= 19           -> "⛈"
        else                      -> "🌤"
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
