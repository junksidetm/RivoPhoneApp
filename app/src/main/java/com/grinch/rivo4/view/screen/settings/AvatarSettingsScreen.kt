package com.grinch.rivo4.view.screen.settings
import com.grinch.rivo4.view.components.MenuTopAppBar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grinch.rivo4.R
import com.grinch.rivo4.controller.util.PreferenceManager
import com.grinch.rivo4.view.components.RivoAvatar
import com.grinch.rivo4.view.components.RivoAvatarShapeSelectorRow
import com.grinch.rivo4.view.components.RivoAvatarStyle
import com.grinch.rivo4.view.theme.rivoAvatarShape
import com.grinch.rivo4.view.components.RivoDivider
import com.grinch.rivo4.view.components.RivoExpressiveCard
import com.grinch.rivo4.view.components.RivoExpressiveGroup
import com.grinch.rivo4.view.components.RivoSwitchListItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun AvatarSettingsScreen(
    navigator: DestinationsNavigator
) {
    val prefs = koinInject<PreferenceManager>()
    val settingsState by prefs.settingsChanged.collectAsState()

    var showPicture by remember(settingsState) {
        mutableStateOf(prefs.getBoolean(PreferenceManager.KEY_SHOW_PICTURE, true))
    }
    var colorfulAvatars by remember(settingsState) {
        mutableStateOf(prefs.getBoolean(PreferenceManager.KEY_COLORFUL_AVATARS, true))
    }
    var gradientAvatars by remember(settingsState) {
        mutableStateOf(prefs.getBoolean(PreferenceManager.KEY_GRADIENT_AVATARS, false))
    }
    var hideAvatarWithBg by remember(settingsState) {
        mutableStateOf(prefs.getBoolean(PreferenceManager.KEY_HIDE_AVATAR_WITH_BACKGROUND, false))
    }
    var groupCalls by remember(settingsState) {
        mutableStateOf(prefs.isCallLogGroupingEnabled())
    }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = stringResource(R.string.settings_avatars_title),
                navigator = navigator
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            item {
                RivoExpressiveGroup(title = "Display Options", icon = Icons.Outlined.AccountCircle) {
                    item {
                        RivoSwitchListItem(
                            headline = stringResource(R.string.settings_interface_show_picture),
                            supporting = stringResource(R.string.settings_interface_show_picture_supporting),
                            leadingIcon = Icons.Outlined.AccountCircle,
                            checked = showPicture,
                            onCheckedChange = {
                                showPicture = it
                                prefs.setBoolean(PreferenceManager.KEY_SHOW_PICTURE, it)
                            }
                        )
                    }
                    item {
                        RivoSwitchListItem(
                            headline = stringResource(R.string.settings_interface_colorful_avatars),
                            supporting = "Smart adaptive contrast: automatically calculates background luminance to display crisp white or dark text for optimal legibility",
                            leadingIcon = Icons.Outlined.Palette,
                            checked = colorfulAvatars,
                            onCheckedChange = {
                                colorfulAvatars = it
                                prefs.setBoolean(PreferenceManager.KEY_COLORFUL_AVATARS, it)
                            }
                        )
                    }
                    item {
                        AnimatedVisibility(
                            visible = colorfulAvatars,
                            enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                            exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) + fadeOut()
                        ) {
                            RivoExpressiveCard(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Smart Contrast Preview",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Text automatically adapts: dark blue backgrounds use crisp white text, and yellow backgrounds use dark text for high legibility.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    val currentShape = rivoAvatarShape(avatarShape)
                                    val contrastPreviewStyle = RivoAvatarStyle(
                                        showPicture = false,
                                        showFirstLetter = true,
                                        colorful = true,
                                        gradient = false,
                                        shapeIndex = avatarShape,
                                        shape = currentShape
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(
                                            Triple("David", "Blue", "White text"),
                                            Triple("Daisy", "Yellow", "Dark text"),
                                            Triple("Alice", "Green", "Dark text"),
                                            Triple("Emma", "Red", "White text")
                                        ).forEach { (contactName, hueName, contrastDesc) ->
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                RivoAvatar(
                                                    name = contactName,
                                                    style = contrastPreviewStyle,
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(hueName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                                Text(contrastDesc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        RivoSwitchListItem(
                            headline = stringResource(R.string.settings_interface_gradient_avatars),
                            supporting = stringResource(R.string.settings_interface_gradient_avatars_supporting),
                            leadingIcon = Icons.Outlined.Gradient,
                            checked = gradientAvatars,
                            onCheckedChange = {
                                gradientAvatars = it
                                prefs.setBoolean(PreferenceManager.KEY_GRADIENT_AVATARS, it)
                            }
                        )
                    }
                    item {
                        AnimatedVisibility(
                            visible = gradientAvatars,
                            enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                            exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)) + fadeOut()
                        ) {
                            RivoExpressiveCard(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Gradient Avatars Preview",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Multi-tone diagonal gradients with 2-letter initials create rich, dynamic contact cards.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    val currentShape = rivoAvatarShape(avatarShape)
                                    val gradientPreviewStyle = RivoAvatarStyle(
                                        showPicture = false,
                                        showFirstLetter = true,
                                        colorful = true,
                                        gradient = true,
                                        shapeIndex = avatarShape,
                                        shape = currentShape
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(
                                            Triple("David Miller", "Blue", "White text"),
                                            Triple("Daisy Evans", "Yellow", "Dark text"),
                                            Triple("Alice Cooper", "Green", "Dark text"),
                                            Triple("Emma Watson", "Red", "White text")
                                        ).forEach { (contactName, hueName, contrastDesc) ->
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                RivoAvatar(
                                                    name = contactName,
                                                    style = gradientPreviewStyle,
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(hueName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                                Text(contrastDesc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        RivoSwitchListItem(
                            headline = stringResource(R.string.settings_interface_hide_avatar_with_bg),
                            supporting = stringResource(R.string.settings_interface_hide_avatar_with_bg_supporting),
                            leadingIcon = Icons.Outlined.AccountCircle,
                            checked = hideAvatarWithBg,
                            onCheckedChange = {
                                hideAvatarWithBg = it
                                prefs.setBoolean(PreferenceManager.KEY_HIDE_AVATAR_WITH_BACKGROUND, it)
                            }
                        )
                    }
                }
            }

            // Group Calls
            item {
                RivoExpressiveGroup(
                    title = "Call History & Recents",
                    icon = Icons.Outlined.Layers
                ) {
                    item {
                        RivoSwitchListItem(
                            headline = "Group calls",
                            supporting = "Group consecutive calls from the same contact or number in recents",
                            leadingIcon = Icons.Outlined.Layers,
                            checked = groupCalls,
                            onCheckedChange = {
                                groupCalls = it
                                prefs.setCallLogGroupingEnabled(it)
                            }
                        )
                    }
                }
            }

            item {
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
