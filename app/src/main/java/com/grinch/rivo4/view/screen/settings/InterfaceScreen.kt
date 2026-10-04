package com.grinch.rivo4.view.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grinch.rivo4.R
import com.grinch.rivo4.controller.util.PreferenceManager
import com.grinch.rivo4.view.components.LiquidGlassPreviewCard
import com.grinch.rivo4.view.components.MenuTopAppBar
import com.grinch.rivo4.view.components.RivoExpressiveCard
import com.grinch.rivo4.view.components.RivoLiquidGlassToggleItem
import com.grinch.rivo4.view.components.RivoListItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ShapeMotionSettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ThemeSettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TypographySettingsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun InterfaceScreen(
    navigator: DestinationsNavigator
) {
    val prefs = koinInject<PreferenceManager>()
    val settingsState by prefs.settingsChanged.collectAsState()
    var uiBlurEnabled by remember(settingsState) {
        mutableStateOf(prefs.isUiBlurEnabled())
    }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = stringResource(R.string.settings_interface_title),
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Theme (Opens ThemeSettingsScreen)
            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = "Theme",
                        supporting = "System, light & dark theme, dynamic color variants & AMOLED",
                        leadingIcon = Icons.Outlined.Palette,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(ThemeSettingsScreenDestination) }
                    )
                }
            }

            // 2. Typography (Opens TypographySettingsScreen)
            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = "Typography",
                        supporting = "Google Sans Flex variable axes, preview & sliders",
                        leadingIcon = Icons.Outlined.FontDownload,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(TypographySettingsScreenDestination) }
                    )
                }
            }

            // 3. Shape & Motion (Opens ShapeMotionSettingsScreen)
            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = stringResource(R.string.settings_group_shape_motion),
                        supporting = "Cards layout, card roundness & transition animations",
                        leadingIcon = Icons.Outlined.ViewAgenda,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(ShapeMotionSettingsScreenDestination) }
                    )
                }
            }

            // 4. Liquid Glass (Inline switch, does not open a new page)
            item {
                RivoExpressiveCard {
                    Column {
                        RivoLiquidGlassToggleItem(
                            checked = uiBlurEnabled,
                            onCheckedChange = {
                                uiBlurEnabled = it
                                prefs.setUiBlurEnabled(it)
                            }
                        )
                        AnimatedVisibility(
                            visible = uiBlurEnabled,
                            enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                            exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) + fadeOut()
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                LiquidGlassPreviewCard()
                            }
                        }
                    }
                }
            }
        }
    }
}
