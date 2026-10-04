package com.grinch.rivo4.view.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Dock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grinch.rivo4.R
import com.grinch.rivo4.view.components.MenuTopAppBar
import com.grinch.rivo4.view.components.RivoExpressiveCard
import com.grinch.rivo4.view.components.RivoListItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AvatarSettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.BottomNavScreenDestination
import com.ramcosta.composedestinations.generated.destinations.InterfaceScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SoundVibrationScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun PersonalizationSettingsScreen(
    navigator: DestinationsNavigator
) {
    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = "Personalization & Display",
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
            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = "Theme & Appearance",
                        supporting = "Theme mode, dynamic color variants, typography & glass blur",
                        leadingIcon = Icons.Outlined.Palette,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(InterfaceScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = "Navigation Bar",
                        supporting = "Floating bar style, blur effect, roundness & tab layout",
                        leadingIcon = Icons.Outlined.Dock,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(BottomNavScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = "Avatars & Contact Cards",
                        supporting = "Circular avatars, contact photos, initials & group calls",
                        leadingIcon = Icons.Outlined.AccountCircle,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(AvatarSettingsScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = stringResource(R.string.settings_sound_vibration_headline),
                        supporting = stringResource(R.string.settings_sound_vibration_supporting),
                        leadingIcon = Icons.AutoMirrored.Outlined.VolumeUp,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(SoundVibrationScreenDestination) }
                    )
                }
            }
        }
    }
}
