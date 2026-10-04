package com.grinch.rivo4.view.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.NotificationImportant
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material.icons.outlined.Swipe
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
import com.ramcosta.composedestinations.generated.destinations.CallAccountsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.CallRecordingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.PriorityContactsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SwipeActionsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun CallingBehaviorSettingsScreen(
    navigator: DestinationsNavigator
) {
    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = "Calling & Behaviour",
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
                        headline = stringResource(R.string.settings_call_settings_headline),
                        supporting = "SIM accounts, dual SIM buttons, dialpad behavior & calling cards",
                        leadingIcon = Icons.Outlined.SimCard,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(CallAccountsScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = stringResource(R.string.settings_swipe_actions_headline),
                        supporting = stringResource(R.string.settings_swipe_actions_supporting),
                        leadingIcon = Icons.Outlined.Swipe,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(SwipeActionsScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = stringResource(R.string.call_recordings_title),
                        supporting = "Auto-recording, Shizuku internal audio & saved recordings",
                        leadingIcon = Icons.Outlined.FiberManualRecord,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(CallRecordingsScreenDestination()) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = stringResource(R.string.priority_contacts_title),
                        supporting = "Starred contacts and custom VIPs with bypass DND",
                        leadingIcon = Icons.Outlined.NotificationImportant,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(PriorityContactsScreenDestination) }
                    )
                }
            }
        }
    }
}
