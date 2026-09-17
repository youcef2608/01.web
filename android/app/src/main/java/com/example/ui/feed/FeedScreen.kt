package com.example.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HelpCall
import com.example.data.model.HelpCallStatus
import com.example.data.model.HelpCategory
import com.example.data.model.UrgencyLevel
import com.example.ui.theme.UrgencyHighColor
import com.example.ui.theme.UrgencyHighContainer
import com.example.ui.theme.UrgencyLowColor
import com.example.ui.theme.UrgencyLowContainer
import com.example.ui.theme.UrgencyMediumColor
import com.example.ui.theme.UrgencyMediumContainer

import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            viewModel.fetchUserLocation()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // شريط التصفية والفئات
                FilterBar(
                    selectedCategory = uiState.selectedCategory,
                    selectedUrgency = uiState.selectedUrgency,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onUrgencySelected = { viewModel.selectUrgency(it) }
                )

                if (!locationPermissionState.status.isGranted) {
                    LocationPermissionRequest(
                        onRequestPermission = { locationPermissionState.launchPermissionRequest() }
                    )
                } else if (uiState.errorMessage != null) {
                    LocationErrorPlaceholder(
                        errorMessage = uiState.errorMessage ?: "",
                        onRetry = { viewModel.fetchUserLocation() }
                    )
                } else if (uiState.calls.isEmpty()) {
                    EmptyFeedPlaceholder()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.calls, key = { it.id }) { call ->
                            HelpCallCard(
                                call = call,
                                userLat = uiState.userLatitude,
                                userLon = uiState.userLongitude,
                                currentUserId = uiState.currentUserId,
                                onVolunteer = { viewModel.volunteerToHelp(call) },
                                onRequestConfirmation = { viewModel.requestConfirmation(call) },
                                onConfirm = { viewModel.confirmCompletion(call) }
                            )
                        }
                    }
                }
            }

            // نافذة حوارية تعرض صدى الأثر المولد عبر Gemini
            uiState.activeEchoNotice?.let { notice ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissNotice() },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "صدى التطوع والتكافل",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissNotice() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("dismiss_echo_dialog_button")
                        ) {
                            Text("استمر في نشر الخير")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FilterBar(
    selectedCategory: HelpCategory?,
    selectedUrgency: UrgencyLevel?,
    onCategorySelected: (HelpCategory?) -> Unit,
    onUrgencySelected: (UrgencyLevel?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 10.dp)
    ) {
        // فئات المساعدة
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("الكل") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("filter_all_categories")
                )
            }
            items(HelpCategory.entries.toTypedArray()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategorySelected(cat) },
                    label = { Text(cat.labelArabic) },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(cat),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("filter_cat_${cat.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // درجات الإلحاح
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.padding(top = 8.dp, end = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "الإلحاح:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(UrgencyLevel.entries.toTypedArray()) { urgency ->
                FilterChip(
                    selected = selectedUrgency == urgency,
                    onClick = { onUrgencySelected(urgency) },
                    label = { Text(urgency.labelArabic) },
                    modifier = Modifier.testTag("filter_urgency_${urgency.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
fun HelpCallCard(
    call: HelpCall,
    userLat: Double,
    userLon: Double,
    currentUserId: String = "",
    onVolunteer: () -> Unit,
    onRequestConfirmation: () -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("help_call_card_${call.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = getCategoryIcon(call.category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = call.category.labelArabic,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = call.formatDistance(userLat, userLon),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    UrgencyBadge(urgency = call.urgency)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = call.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = call.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${call.locationName} • بواسطة ${call.authorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusIndicator(status = call.status)

                when (call.status) {
                    HelpCallStatus.OPEN -> {
                        Button(
                            onClick = onVolunteer,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("volunteer_button_${call.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "أنا سأساعد",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    HelpCallStatus.IN_PROGRESS -> {
                        // Only the volunteer should ideally see this button, but for simplicity of UI without full auth check here, we show it.
                        Button(
                            onClick = onRequestConfirmation,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("request_confirmation_button_${call.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("انتهيت، اطلب التأكيد")
                        }
                    }
                    HelpCallStatus.PENDING_CONFIRMATION -> {
                        if (currentUserId == call.requesterId) { 
                            Button(
                                onClick = onConfirm,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("confirm_button_${call.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تأكيد إتمام المساعدة ✅")
                            }
                        } else {
                             Text(
                                text = "بانتظار تأكيد صاحب النداء",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    HelpCallStatus.COMPLETED -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "أُحدث الأثر ✨",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    HelpCallStatus.CANCELLED -> {
                         Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "أُلغي",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UrgencyBadge(urgency: UrgencyLevel) {
    val (bgColor, textColor) = when (urgency) {
        UrgencyLevel.LOW -> Pair(UrgencyLowContainer, UrgencyLowColor)
        UrgencyLevel.MEDIUM -> Pair(UrgencyMediumContainer, UrgencyMediumColor)
        UrgencyLevel.URGENT -> Pair(UrgencyHighContainer, UrgencyHighColor)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = urgency.labelArabic,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatusIndicator(status: HelpCallStatus) {
    val (dotColor, text) = when (status) {
        HelpCallStatus.OPEN -> Pair(Color(0xFF2E7D32), status.labelArabic)
        HelpCallStatus.IN_PROGRESS -> Pair(Color(0xFFE65100), status.labelArabic)
        HelpCallStatus.COMPLETED -> Pair(Color(0xFF546E7A), status.labelArabic)
        HelpCallStatus.PENDING_CONFIRMATION -> Pair(Color(0xFFF57C00), status.labelArabic)
        HelpCallStatus.CANCELLED -> Pair(Color(0xFFB0BEC5), status.labelArabic)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

fun getCategoryIcon(category: HelpCategory): ImageVector = when (category) {
    HelpCategory.DELIVERY -> Icons.Default.LocalShipping
    HelpCategory.COMPANIONSHIP -> Icons.Default.People
    HelpCategory.EDUCATION -> Icons.Default.School
    HelpCategory.MOVING -> Icons.Default.Inventory2
    HelpCategory.OTHER -> Icons.Default.VolunteerActivism
}

@Composable
fun EmptyFeedPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.VolunteerActivism,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "لا توجد نداءات حالية تطابق التصفية",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "كن أول من يبادر بنشر نداء مساعدة لجيرانك في الحي.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LocationPermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "نحتاج صلاحية الموقع",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "يتطلب تطبيق لمّة الوصول إلى موقعك الجغرافي لعرض نداءات المساعدة القريبة منك في الوقت الفعلي ولحساب المسافة بدقة.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onRequestPermission) {
            Text("منح صلاحية الموقع")
        }
    }
}

@Composable
fun LocationErrorPlaceholder(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "تعذر تحديد الموقع",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(onClick = onRetry) {
            Text("إعادة المحاولة")
        }
    }
}

