package com.grinch.rivo4.view.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.PhoneCallback
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grinch.rivo4.R
import com.grinch.rivo4.controller.util.PreferenceManager
import com.grinch.rivo4.view.components.MenuTopAppBar
import com.grinch.rivo4.view.components.RivoExpressiveCard
import com.grinch.rivo4.view.components.RivoListItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AppLockScreenDestination
import com.ramcosta.composedestinations.generated.destinations.BlockedNumbersScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FakeCallSchedulerScreenDestination
import com.ramcosta.composedestinations.generated.destinations.PermissionsChecklistScreenDestination
import com.ramcosta.composedestinations.generated.destinations.PrivateContactsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun SecuritySettingsScreen(
    navigator: DestinationsNavigator
) {
    val prefs = koinInject<PreferenceManager>()
    val settingsState by prefs.settingsChanged.collectAsState()
    val appLockEnabled = remember(settingsState) { prefs.isAppLockEnabled() }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = "Security",
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
                        headline = "App Lock",
                        supporting = if (appLockEnabled) "Enabled (Face, Fingerprint, PIN)" else "Protect app with biometrics or PIN",
                        leadingIcon = Icons.Outlined.Lock,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(AppLockScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = "Private Storage",
                        supporting = "Secret dialpad vault • Stored only in app memory",
                        leadingIcon = Icons.Outlined.FolderShared,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(PrivateContactsScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = stringResource(R.string.settings_blocked_numbers_headline),
                        supporting = stringResource(R.string.settings_blocked_numbers_supporting),
                        leadingIcon = Icons.Outlined.Block,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(BlockedNumbersScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = stringResource(R.string.fake_call_title),
                        supporting = stringResource(R.string.fake_call_subtitle),
                        leadingIcon = Icons.AutoMirrored.Outlined.PhoneCallback,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(FakeCallSchedulerScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = "Permissions & App Setup",
                        supporting = "Review granted permissions and system capabilities",
                        leadingIcon = Icons.Outlined.VerifiedUser,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(PermissionsChecklistScreenDestination) }
                    )
                }
            }
        }
    }
}
