package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.AppModule
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.LoginViewModel
import com.example.ui.feed.FeedScreen
import com.example.ui.feed.FeedViewModel
import com.example.ui.leaderboard.LeaderboardScreen
import com.example.ui.leaderboard.LeaderboardViewModel
import com.example.ui.location.LocationSetupScreen
import com.example.ui.location.LocationSetupViewModel
import com.example.ui.map.MapScreen
import com.example.ui.map.MapViewModel
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.ProfileViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel

enum class MainDestination(
    val route: String,
    val titleArabic: String,
    val icon: ImageVector
) {
    FEED("feed", "النداءات", Icons.Default.VolunteerActivism),
    MAP("map", "الخريطة", Icons.Default.Map),
    LEADERBOARD("leaderboard", "الصدارة", Icons.Default.EmojiEvents),
    PROFILE("profile", "أثري", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchdaMainScreen(modifier: Modifier = Modifier) {
    var currentDestination by rememberSaveable { mutableStateOf(MainDestination.FEED) }
    var isLoginOpen by rememberSaveable { mutableStateOf(false) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }

    val container = AppModule.container

    val feedViewModel: FeedViewModel = viewModel(
        factory = FeedViewModel.provideFactory(
            container.helpCallRepository,
            container.volunteerRepository,
            container.geminiRepository,
            container.locationRepository
        )
    )

    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModel.provideFactory(
            container.helpCallRepository,
            container.volunteerRepository
        )
    )

    val leaderboardViewModel: LeaderboardViewModel = viewModel(
        factory = LeaderboardViewModel.provideFactory(
            container.volunteerRepository
        )
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(
            container.volunteerRepository,
            container.helpCallRepository
        )
    )

    val locationSetupViewModel: LocationSetupViewModel = viewModel(
        factory = LocationSetupViewModel.provideFactory(
            container.locationRepository,
            container.volunteerRepository
        )
    )

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.provideFactory(
            container.volunteerRepository
        )
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            container.volunteerRepository,
            container.locationRepository
        )
    )

    val isUserLoggedIn by container.volunteerRepository.isUserLoggedIn.collectAsState()
    val isLocationSetupComplete by container.volunteerRepository.isLocationSetupComplete.collectAsState()

    var isSplashVisible by rememberSaveable { mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1200)
        isSplashVisible = false
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (isSplashVisible) {
            LammaSplashScreen()
        } else if (isLoginOpen) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { isLoginOpen = false },
                onNavigateBack = { isLoginOpen = false }
            )
        } else if (isSettingsOpen) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { isSettingsOpen = false },
                onNavigateToLogin = {
                    isSettingsOpen = false
                    isLoginOpen = true
                }
            )
        } else if (isUserLoggedIn && !isLocationSetupComplete) {
            LocationSetupScreen(
                viewModel = locationSetupViewModel,
                onLocationSaved = { /* Will navigate back to main automatically due to state change */ }
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "لمّة",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "تكافل مجتمعي في الوقت الفعلي",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        },
                        actions = {
                            if (!isUserLoggedIn) {
                                TextButton(
                                    onClick = { isLoginOpen = true },
                                    modifier = Modifier.testTag("topbar_login_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "دخول",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(
                                onClick = { isSettingsOpen = true },
                                modifier = Modifier.testTag("topbar_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "الإعدادات",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("app_top_bar")
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        MainDestination.entries.forEach { destination ->
                            val selected = currentDestination == destination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.titleArabic
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.titleArabic,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_item_${destination.route}")
                            )
                        }
                    }
                },
                modifier = modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentDestination) {
                        MainDestination.FEED -> FeedScreen(viewModel = feedViewModel)
                        MainDestination.MAP -> MapScreen(
                            viewModel = mapViewModel
                        )
                        MainDestination.LEADERBOARD -> LeaderboardScreen(
                            viewModel = leaderboardViewModel
                        )
                        MainDestination.PROFILE -> ProfileScreen(
                            viewModel = profileViewModel,
                            onOpenLogin = { isLoginOpen = true },
                            onOpenSettings = { isSettingsOpen = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LammaSplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        Color(0xFFE85835),
                        Color(0xFFEA580C)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = "لمّة",
                        tint = Color(0xFFE85835),
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "لمّة",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "تكافل مجتمعي في الوقت الفعلي",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White.copy(alpha = 0.8f),
                strokeWidth = 2.5.dp
            )
        }
    }
}
