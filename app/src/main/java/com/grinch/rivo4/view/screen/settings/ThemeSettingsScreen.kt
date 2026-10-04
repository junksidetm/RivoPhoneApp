package com.grinch.rivo4.view.screen.settings

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grinch.rivo4.R
import com.grinch.rivo4.controller.util.PreferenceManager
import com.grinch.rivo4.view.components.*
import com.grinch.rivo4.view.theme.RivoMaterialShapes
import com.grinch.rivo4.view.theme.RivoMotion
import com.grinch.rivo4.view.theme.rememberRivoMorphShape
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.compose.koinInject

data class DynamicVariantInfo(
    val id: Int,
    val name: String,
    val description: String,
    val sampleColors: List<Color>
)

val DYNAMIC_VARIANTS = listOf(
    DynamicVariantInfo(0, "Tonal Spot", "Balanced classic Material 3 default", listOf(Color(0xFF6750A4), Color(0xFF7D5260), Color(0xFF625B71))),
    DynamicVariantInfo(1, "Expressive", "High chroma, energetic & warm highlights", listOf(Color(0xFF7E5265), Color(0xFF8C4F27), Color(0xFF5B624C))),
    DynamicVariantInfo(2, "Neutral", "Subtle, low-saturation calm tones", listOf(Color(0xFF5F5E5E), Color(0xFF605D62), Color(0xFF5D5E61))),
    DynamicVariantInfo(3, "Vibrant", "Maximum vividness & punchy accents", listOf(Color(0xFF0061A4), Color(0xFF535F70), Color(0xFF6B5778))),
    DynamicVariantInfo(4, "Fruit Salad", "Playful berry, melon & citrus contrast", listOf(Color(0xFF006A60), Color(0xFF4A635F), Color(0xFF456179))),
    DynamicVariantInfo(5, "Rainbow", "Rich multi-spectral color distribution", listOf(Color(0xFF984061), Color(0xFF705574), Color(0xFF8C4A60))),
    DynamicVariantInfo(6, "Content", "True fidelity matching wallpaper content", listOf(Color(0xFF436916), Color(0xFF57624A), Color(0xFF386663))),
    DynamicVariantInfo(7, "Fidelity", "Strict seed color preservation", listOf(Color(0xFF006874), Color(0xFF4A6267), Color(0xFF525E7D))),
    DynamicVariantInfo(8, "Monochrome", "Clean black, white & grayscale aesthetics", listOf(Color(0xFF303030), Color(0xFF606060), Color(0xFF909090))),
    DynamicVariantInfo(9, "Big Clock", "High-contrast bold Android 14 clock style", listOf(Color(0xFF9C4146), Color(0xFF775656), Color(0xFF755A2F))),
    DynamicVariantInfo(10, "Candy", "Pastel confectionery tones", listOf(Color(0xFFB52750), Color(0xFF7B5267), Color(0xFF765667))),
    DynamicVariantInfo(11, "Deep Ocean", "Cool aquatic navy, teal & cyan blend", listOf(Color(0xFF00658E), Color(0xFF4F606E), Color(0xFF63597C))),
    DynamicVariantInfo(12, "Sunset Glow", "Warm golden amber & twilight coral", listOf(Color(0xFF8B5000), Color(0xFF715B41), Color(0xFF56643C)))
)

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun ThemeSettingsScreen(
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val prefs = koinInject<PreferenceManager>()
    val settingsState by prefs.settingsChanged.collectAsState()

    var themeMode by remember(settingsState) {
        mutableIntStateOf(prefs.getThemeMode())
    }
    var dynamicColors by remember(settingsState) {
        mutableStateOf(prefs.getBoolean(PreferenceManager.KEY_DYNAMIC_COLORS, true))
    }
    var selectedVariant by remember(settingsState) {
        mutableIntStateOf(prefs.getDynamicColorVariant())
    }
    var amoledMode by remember(settingsState) {
        mutableStateOf(prefs.getBoolean(PreferenceManager.KEY_AMOLED_MODE, false))
    }
    var customPrimaryColor by remember(settingsState) {
        mutableIntStateOf(prefs.getInt("custom_primary_color", Color(0xFF6750A4).toArgb()))
    }
    var showColorPickerDialog by remember { mutableStateOf(false) }

    val presetColors = listOf(
        Color(0xFF6750A4), Color(0xFF0061A4), Color(0xFF006A60),
        Color(0xFF436916), Color(0xFF984061), Color(0xFF808080)
    )

    fun triggerRestart() {
        (context as? Activity)?.recreate()
    }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = "Theme",
                navigator = navigator
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Theme Mode
            item {
                RivoExpressiveCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Theme Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf(
                                Triple(PreferenceManager.THEME_MODE_SYSTEM, "System", Icons.Outlined.BrightnessAuto),
                                Triple(PreferenceManager.THEME_MODE_LIGHT, "Light", Icons.Outlined.LightMode),
                                Triple(PreferenceManager.THEME_MODE_DARK, "Dark", Icons.Outlined.DarkMode)
                            )
                            modes.forEach { (mode, title, icon) ->
                                val selected = themeMode == mode
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            themeMode = mode
                                            prefs.setThemeMode(mode)
                                            triggerRestart()
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Dynamic Color & Variants
            item {
                RivoExpressiveCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RivoSwitchListItem(
                            headline = "Dynamic Color",
                            supporting = "Derive colors automatically from wallpaper (Material You)",
                            leadingIcon = Icons.Outlined.Palette,
                            checked = dynamicColors,
                            onCheckedChange = {
                                dynamicColors = it
                                prefs.setBoolean(PreferenceManager.KEY_DYNAMIC_COLORS, it)
                                triggerRestart()
                            }
                        )

                        if (!dynamicColors) {
                            Text(
                                text = "Accent Color",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val isCustomActive = presetColors.none { it.toArgb() == customPrimaryColor }
                            RivoColorSwatchRow(
                                colors = presetColors,
                                selectedColor = if (!isCustomActive) presetColors.firstOrNull { it.toArgb() == customPrimaryColor } else null,
                                onColorSelected = { color ->
                                    customPrimaryColor = color.toArgb()
                                    prefs.setInt("custom_primary_color", color.toArgb())
                                    triggerRestart()
                                },
                                trailingContent = {
                                    val customColor = remember(customPrimaryColor) { Color(customPrimaryColor) }
                                    Surface(
                                        selected = isCustomActive,
                                        onClick = { showColorPickerDialog = true },
                                        modifier = Modifier.size(44.dp),
                                        shape = CircleShape,
                                        color = if (isCustomActive) customColor else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        contentColor = if (isCustomActive) Color.White else MaterialTheme.colorScheme.primary,
                                        border = if (!isCustomActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant) else null
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isCustomActive) Icons.Default.Check else Icons.Outlined.Colorize,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 3. Dynamic Variants (When Dynamic Color is ON)
            if (dynamicColors) {
                item {
                    Text(
                        text = "DYNAMIC VARIANTS (13 PALETTES)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                DYNAMIC_VARIANTS.forEach { variant ->
                    item(key = variant.id) {
                        val isSelected = selectedVariant == variant.id
                        RivoExpressiveCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedVariant = variant.id
                                    prefs.setDynamicColorVariant(variant.id)
                                    triggerRestart()
                                },
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                    variant.sampleColors.forEach { col ->
                                        Surface(
                                            modifier = Modifier.size(24.dp),
                                            shape = CircleShape,
                                            color = col,
                                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface)
                                        ) {}
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = variant.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = variant.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. AMOLED Dark Mode
            item {
                RivoExpressiveCard {
                    RivoSwitchListItem(
                        headline = stringResource(R.string.settings_interface_amoled),
                        supporting = stringResource(R.string.settings_interface_amoled_supporting),
                        leadingIcon = Icons.Outlined.DarkMode,
                        checked = amoledMode,
                        onCheckedChange = {
                            amoledMode = it
                            prefs.setBoolean(PreferenceManager.KEY_AMOLED_MODE, it)
                            triggerRestart()
                        }
                    )
                }
            }
        }

        if (showColorPickerDialog) {
            ColorPickerDialog(
                currentColor = Color(customPrimaryColor),
                onColorSelected = { color ->
                    customPrimaryColor = color.toArgb()
                    prefs.setInt("custom_primary_color", color.toArgb())
                    showColorPickerDialog = false
                    triggerRestart()
                },
                onDismissRequest = { showColorPickerDialog = false }
            )
        }
    }
}
