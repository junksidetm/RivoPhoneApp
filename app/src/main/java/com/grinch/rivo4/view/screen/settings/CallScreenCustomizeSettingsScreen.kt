package com.grinch.rivo4.view.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grinch.rivo4.controller.util.PreferenceManager
import com.grinch.rivo4.view.components.MenuTopAppBar
import com.grinch.rivo4.view.components.RivoExpressiveCard
import com.grinch.rivo4.view.components.RivoSwitchListItem
import com.grinch.rivo4.view.theme.createGoogleSansFlexFamily
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.compose.koinInject

private data class CallButtonItem(
    val key: String,
    val label: String,
    val icon: ImageVector
)

private val ALL_CALL_BUTTONS = listOf(
    CallButtonItem("mute", "Mute / Unmute", Icons.Default.MicOff),
    CallButtonItem("keypad", "Keypad", Icons.Default.Dialpad),
    CallButtonItem("audio", "Audio Route", Icons.AutoMirrored.Filled.VolumeUp),
    CallButtonItem("record", "Call Recording", Icons.Default.FiberManualRecord),
    CallButtonItem("hold", "Hold / Resume", Icons.Default.Pause),
    CallButtonItem("add_call", "Add / Merge Call", Icons.Default.Add)
)

private val BUTTON_MAP = ALL_CALL_BUTTONS.associateBy { it.key }

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun CallScreenCustomizeSettingsScreen(
    navigator: DestinationsNavigator
) {
    val prefs = koinInject<PreferenceManager>()
    val settingsState by prefs.settingsChanged.collectAsState()

    var customFontEnabled by remember(settingsState) {
        mutableStateOf(prefs.isCallNameCustomFontEnabled())
    }
    var fontWeight by remember(settingsState) {
        mutableIntStateOf(prefs.getCallNameWeight())
    }
    var fontWidth by remember(settingsState) {
        mutableFloatStateOf(prefs.getCallNameWidth())
    }
    var fontGrade by remember(settingsState) {
        mutableFloatStateOf(prefs.getCallNameGrade())
    }
    var fontRoundness by remember(settingsState) {
        mutableFloatStateOf(prefs.getCallNameRoundness())
    }
    var fontOpticalSize by remember(settingsState) {
        mutableFloatStateOf(prefs.getCallNameOpticalSize())
    }
    var fontSlant by remember(settingsState) {
        mutableFloatStateOf(prefs.getCallNameSlant())
    }
    var buttonOrder by remember(settingsState) {
        mutableStateOf(prefs.getCallButtonOrder())
    }

    val previewFontFamily = remember(customFontEnabled, fontWeight, fontWidth, fontGrade, fontRoundness, fontOpticalSize, fontSlant) {
        if (customFontEnabled) {
            createGoogleSansFlexFamily(
                weight = fontWeight,
                width = fontWidth,
                grade = fontGrade,
                roundness = fontRoundness,
                opticalSize = fontOpticalSize,
                slant = fontSlant
            )
        } else {
            FontFamily.Default
        }
    }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = "Call Screen Customization",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Live Preview of Caller Screen
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "JD",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Jane Doe",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = previewFontFamily,
                                fontWeight = FontWeight(fontWeight)
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "+1 (555) 123-4567",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mini representation of active call buttons grid
                        val row1 = buttonOrder.take(3)
                        val row2 = buttonOrder.drop(3).take(3)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row1.forEach { key ->
                                val btn = BUTTON_MAP[key]
                                if (btn != null) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = btn.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = btn.label.take(6),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row2.forEach { key ->
                                val btn = BUTTON_MAP[key]
                                if (btn != null) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = btn.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = btn.label.take(6),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full-width circular End Call button preview
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDC2626),
                            contentColor = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 2. Caller Name Typography Section
            item {
                RivoExpressiveCard {
                    RivoSwitchListItem(
                        headline = "Custom Caller Name Typography",
                        supporting = "Apply Google Sans Flex variable font axes to caller name",
                        leadingIcon = Icons.Outlined.FontDownload,
                        checked = customFontEnabled,
                        onCheckedChange = { enabled ->
                            customFontEnabled = enabled
                            prefs.setCallNameCustomFontEnabled(enabled)
                        }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = customFontEnabled,
                    enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RivoTypographySliderTile(
                            label = "Grade",
                            code = "GRAD",
                            value = fontGrade,
                            min = -200f,
                            max = 150f,
                            displayValue = fontGrade.toInt().toString(),
                            onValueChange = {
                                fontGrade = it
                                prefs.setCallNameGrade(it)
                            }
                        )
                        RivoTypographySliderTile(
                            label = "Weight",
                            code = "wght",
                            value = fontWeight.toFloat(),
                            min = 100f,
                            max = 1000f,
                            displayValue = fontWeight.toString(),
                            onValueChange = {
                                fontWeight = it.toInt()
                                prefs.setCallNameWeight(it.toInt())
                            }
                        )
                        RivoTypographySliderTile(
                            label = "Width",
                            code = "wdth",
                            value = fontWidth,
                            min = 25f,
                            max = 151f,
                            displayValue = "${fontWidth.toInt()}%",
                            onValueChange = {
                                fontWidth = it
                                prefs.setCallNameWidth(it)
                            }
                        )
                        RivoTypographySliderTile(
                            label = "Roundness",
                            code = "ROND",
                            value = fontRoundness,
                            min = 0f,
                            max = 100f,
                            displayValue = "${fontRoundness.toInt()}%",
                            onValueChange = {
                                fontRoundness = it
                                prefs.setCallNameRoundness(it)
                            }
                        )
                        RivoTypographySliderTile(
                            label = "Optical Size",
                            code = "opsz",
                            value = fontOpticalSize,
                            min = 6f,
                            max = 144f,
                            displayValue = "${fontOpticalSize.toInt()}pt",
                            onValueChange = {
                                fontOpticalSize = it
                                prefs.setCallNameOpticalSize(it)
                            }
                        )
                        RivoTypographySliderTile(
                            label = "Slant",
                            code = "slnt",
                            value = fontSlant,
                            min = -10f,
                            max = 0f,
                            displayValue = "${fontSlant.toInt()}°",
                            onValueChange = {
                                fontSlant = it
                                prefs.setCallNameSlant(it)
                            }
                        )
                    }
                }
            }

            // 3. Rearrange Active Call Buttons Section
            item {
                Text(
                    text = "Rearrange Call Buttons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
                Text(
                    text = "Top row: items 1–3 • Bottom row: items 4–6",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            items(buttonOrder.size) { index ->
                val key = buttonOrder[index]
                val item = BUTTON_MAP[key] ?: CallButtonItem(key, key, Icons.Outlined.Tune)
                val isTopRow = index < 3
                val rowPosition = (index % 3) + 1

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isTopRow) "Top Row • Slot #$rowPosition" else "Bottom Row • Slot #$rowPosition",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = {
                                if (index > 0) {
                                    val newOrder = buttonOrder.toMutableList()
                                    val temp = newOrder[index]
                                    newOrder[index] = newOrder[index - 1]
                                    newOrder[index - 1] = temp
                                    buttonOrder = newOrder
                                    prefs.setCallButtonOrder(newOrder)
                                }
                            },
                            enabled = index > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move Up"
                            )
                        }

                        IconButton(
                            onClick = {
                                if (index < buttonOrder.size - 1) {
                                    val newOrder = buttonOrder.toMutableList()
                                    val temp = newOrder[index]
                                    newOrder[index] = newOrder[index + 1]
                                    newOrder[index + 1] = temp
                                    buttonOrder = newOrder
                                    prefs.setCallButtonOrder(newOrder)
                                }
                            },
                            enabled = index < buttonOrder.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move Down"
                            )
                        }
                    }
                }
            }

            // 4. Reset Button
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        prefs.resetCallScreenCustomization()
                        customFontEnabled = prefs.isCallNameCustomFontEnabled()
                        fontWeight = prefs.getCallNameWeight()
                        fontWidth = prefs.getCallNameWidth()
                        fontGrade = prefs.getCallNameGrade()
                        fontRoundness = prefs.getCallNameRoundness()
                        fontOpticalSize = prefs.getCallNameOpticalSize()
                        fontSlant = prefs.getCallNameSlant()
                        buttonOrder = prefs.getCallButtonOrder()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Call Screen Defaults",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
