package com.grinch.rivo4.view.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Visibility
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
import com.grinch.rivo4.view.components.RivoSwitchListItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.BackupRestoreScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ContactManagementScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ContactVisibilityScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TrashContactsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun StorageSettingsScreen(
    navigator: DestinationsNavigator
) {
    val prefs = koinInject<PreferenceManager>()
    val settingsState by prefs.settingsChanged.collectAsState()
    var isContactManagementCardEnabled by remember(settingsState) {
        mutableStateOf(prefs.isContactManagementCardEnabled())
    }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = "Storage",
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
                        headline = stringResource(R.string.settings_contact_management_headline),
                        supporting = stringResource(R.string.settings_contact_management_supporting),
                        leadingIcon = Icons.Outlined.ManageAccounts,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(ContactManagementScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoSwitchListItem(
                        headline = stringResource(R.string.settings_contact_management_card),
                        supporting = stringResource(R.string.settings_contact_management_card_supporting),
                        leadingIcon = Icons.Outlined.Info,
                        checked = isContactManagementCardEnabled,
                        onCheckedChange = {
                            isContactManagementCardEnabled = it
                            prefs.setContactManagementCardEnabled(it)
                        }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = stringResource(R.string.settings_manage_visibility),
                        supporting = stringResource(R.string.settings_manage_visibility_supporting),
                        leadingIcon = Icons.Outlined.Visibility,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(ContactVisibilityScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = stringResource(R.string.settings_backup_restore_headline),
                        supporting = stringResource(R.string.settings_backup_restore_supporting),
                        leadingIcon = Icons.Outlined.Backup,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(BackupRestoreScreenDestination) }
                    )
                }
            }

            item {
                RivoExpressiveCard {
                    RivoListItem(
                        headline = "Trash Contacts",
                        supporting = "View and restore recently deleted contacts within 30 days",
                        leadingIcon = Icons.Outlined.Delete,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(TrashContactsScreenDestination) }
                    )
                }
            }
        }
    }
}
