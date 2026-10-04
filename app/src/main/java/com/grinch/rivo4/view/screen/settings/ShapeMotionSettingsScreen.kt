package com.grinch.rivo4.view.screen.settings

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grinch.rivo4.R
import com.grinch.rivo4.controller.util.PreferenceManager
import com.grinch.rivo4.view.components.MenuTopAppBar
import com.grinch.rivo4.view.components.RivoExpressiveCard
import com.grinch.rivo4.view.components.RivoInteractiveRoundnessSlider
import com.grinch.rivo4.view.components.RivoSwitchListItem
import com.grinch.rivo4.view.components.RivoVisualOptionSelectorRow
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun ShapeMotionSettingsScreen(
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val prefs = koinInject<PreferenceManager>()
    val settingsState by prefs.settingsChanged.collectAsState()

    var showCards by remember(settingsState) {
        mutableStateOf(prefs.getBoolean(PreferenceManager.KEY_SHOW_CARDS, true))
    }
    var cardRoundness by remember(settingsState) {
        mutableIntStateOf(prefs.getInt(PreferenceManager.KEY_CARD_ROUNDNESS, 28).coerceAtLeast(5))
    }
    var transitionStyle by remember(settingsState) {
        mutableIntStateOf(prefs.getInt(PreferenceManager.KEY_TRANSITION_STYLE, 0))
    }

    fun triggerRestart() {
        (context as? Activity)?.recreate()
    }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = "Shape & Motion",
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
            item {
                RivoExpressiveCard {
                    RivoSwitchListItem(
                        headline = stringResource(R.string.settings_interface_use_cards),
                        supporting = stringResource(R.string.settings_interface_use_cards_supporting),
                        leadingIcon = Icons.Outlined.ViewAgenda,
                        checked = showCards,
                        onCheckedChange = {
                            showCards = it
                            prefs.setBoolean(PreferenceManager.KEY_SHOW_CARDS, it)
                        }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    Box(modifier = Modifier.padding(16.dp)) {
                        RivoInteractiveRoundnessSlider(
                            headline = stringResource(R.string.settings_interface_card_roundness),
                            supporting = stringResource(R.string.settings_interface_card_roundness_supporting),
                            value = cardRoundness.toFloat().coerceIn(5f, 32f),
                            valueRange = 5f..32f,
                            steps = 26,
                            onValueChange = { cardRoundness = it.roundToInt().coerceAtLeast(5) },
                            onValueChangeFinished = {
                                prefs.setInt(PreferenceManager.KEY_CARD_ROUNDNESS, cardRoundness.coerceAtLeast(5))
                            }
                        )
                    }
                }
            }

            item {
                RivoExpressiveCard {
                    Box(modifier = Modifier.padding(16.dp)) {
                        RivoVisualOptionSelectorRow(
                            headline = stringResource(R.string.settings_interface_transition_animation),
                            supporting = stringResource(R.string.settings_interface_transition_animation_supporting),
                            leadingIcon = Icons.Outlined.Animation,
                            options = listOf(
                                stringResource(R.string.option_standard) to 0,
                                stringResource(R.string.settings_interface_transition_slide) to 1,
                                stringResource(R.string.settings_interface_transition_fade) to 2,
                                stringResource(R.string.settings_interface_transition_none) to 3
                            ),
                            selectedValue = transitionStyle,
                            onValueChange = {
                                transitionStyle = it
                                prefs.setInt(PreferenceManager.KEY_TRANSITION_STYLE, it)
                                triggerRestart()
                            }
                        ) { value, selected ->
                            val icon = when (value) {
                                0 -> Icons.Outlined.Animation
                                1 -> Icons.AutoMirrored.Outlined.CompareArrows
                                2 -> Icons.Outlined.AutoAwesome
                                else -> Icons.Outlined.Block
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
