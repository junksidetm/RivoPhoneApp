package com.grinch.rivo4.view.screen.settings

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.grinch.rivo4.DISCORD_URL
import com.grinch.rivo4.GITHUB_URL
import com.grinch.rivo4.GITLAB_URL
import com.grinch.rivo4.CODEBERG_URL
import com.grinch.rivo4.PATREON_URL
import com.grinch.rivo4.R
import com.grinch.rivo4.controller.util.PreferenceManager
import com.grinch.rivo4.controller.util.getAppVersion
import com.grinch.rivo4.controller.util.openLink
import com.grinch.rivo4.view.components.MenuTopAppBar
import com.grinch.rivo4.view.components.RivoExpressiveCard
import com.grinch.rivo4.view.components.RivoListItem
import com.grinch.rivo4.view.components.TipJarDialog
import com.grinch.rivo4.view.theme.RivoMaterialShapes
import com.grinch.rivo4.view.theme.rememberRivoMorphShape
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ContributorsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun AboutScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val prefs = koinInject<PreferenceManager>()
    val settingsState by prefs.settingsChanged.collectAsState()
    val isSupporter = remember(settingsState) { prefs.isSupporter() }
    var showTipJarDialog by remember { mutableStateOf(false) }
    val appInfo = getAppVersion(context)
    val logoMorph = rememberRivoMorphShape(RivoMaterialShapes.Cookie12Sided, RivoMaterialShapes.Circle) { 0.25f }

    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    fun onVersionTap() {
        val now = System.currentTimeMillis()
        if (now - lastTapTime > 3000L) {
            tapCount = 0
        }
        lastTapTime = now
        tapCount++

        if (tapCount >= 7) {
            tapCount = 0
            Toast.makeText(context, "🎉 You found the Rivo Developer Easter Egg!", Toast.LENGTH_SHORT).show()
        } else if (tapCount >= 3) {
            val remaining = 7 - tapCount
            Toast.makeText(context, "You are $remaining step${if (remaining > 1) "s" else ""} away from the secret", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            MenuTopAppBar(
                text = stringResource(R.string.about_title),
                navigator = navigator
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. App Icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(14.dp)) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = stringResource(R.string.about_logo_content_desc),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 2. App Name & Version Chip
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Rivo",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )

                Surface(
                    onClick = { onVersionTap() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Version ${appInfo.first} (${appInfo.second})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Rebuild from ground up to support Android Community with modern Material 3 Expressive support",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 3. Developer Segmented Card (Inspired by Wallet-Flutter)
            RivoExpressiveCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openLink(context, "https://github.com/mrdarksidetm") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Abhijeet Yadav",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Developer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Pill Buttons (GitHub & Email)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(21.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = { openLink(context, "https://github.com/mrdarksidetm") }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GitHub",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(21.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = { openLink(context, "mailto:contact.dsidetm@gmail.com") }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Email",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 4. Links & Action Cards Group
            RivoExpressiveCard {
                Column {
                    RivoListItem(
                        headline = stringResource(R.string.about_check_updates),
                        supporting = "v${appInfo.first} • Check latest GitHub releases",
                        leadingIcon = Icons.Outlined.SystemUpdate,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { openLink(context, "$GITHUB_URL/releases") }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    RivoListItem(
                        headline = "Open Source",
                        supporting = "View source repository on GitHub",
                        leadingIcon = Icons.Outlined.Code,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { openLink(context, GITHUB_URL) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    RivoListItem(
                        headline = stringResource(R.string.contributors_title),
                        supporting = "People who helped make Rivo possible",
                        leadingIcon = Icons.Outlined.Groups,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { navigator.navigate(ContributorsScreenDestination) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    RivoListItem(
                        headline = if (isSupporter) stringResource(R.string.about_supporter_badge) else stringResource(R.string.about_patreon),
                        supporting = if (isSupporter) "Thank you for supporting Rivo!" else "Support development via Tip Jar or Patreon",
                        leadingIcon = if (isSupporter) Icons.Default.Star else Icons.Default.Favorite,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { showTipJarDialog = true }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    RivoListItem(
                        headline = "Community",
                        supporting = "Join Rivo Phone discussions on Discord",
                        leadingIcon = Icons.AutoMirrored.Filled.Chat,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = { openLink(context, DISCORD_URL) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    RivoListItem(
                        headline = "Licenses",
                        supporting = "Third-party open-source software libraries",
                        leadingIcon = Icons.Outlined.Gavel,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = {
                            try {
                                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                            } catch (_: Exception) {
                                openLink(context, "$GITHUB_URL/blob/main/LICENSE")
                            }
                        }
                    )
                }
            }

            // 5. Footer (Made with ❤️ + Device architecture)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "Made with ❤️",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )
                Text(
                    text = "Android ${Build.VERSION.RELEASE} • ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        if (showTipJarDialog) {
            TipJarDialog(onDismissRequest = { showTipJarDialog = false })
        }
    }
}
