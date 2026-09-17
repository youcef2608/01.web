package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

// خيارات الصور الرمزية المتاحة لتخصيص الحساب بهوية تطوعية دافئة
val volunteerAvatarOptions = listOf(
    "🤝" to "المعوان",
    "🌟" to "المبادر",
    "💚" to "القلب الطيب",
    "🚀" to "صاحب الهمة",
    "🏡" to "جار الخير",
    "✨" to "الأثر الطيب"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "الإعدادات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("settings_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // بطاقة ملخص الحساب الشخصي
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_profile_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarEmoji = uiState.volunteer.avatarUrl.ifBlank { "🤝" }
                            Text(
                                text = avatarEmoji,
                                fontSize = 28.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.volunteer.name.ifBlank { "متطوع لمّة" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (uiState.volunteer.phoneNumber.isNotBlank()) uiState.volunteer.phoneNumber else "لم يتم تسجيل رقم الهاتف",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = "${uiState.volunteer.echoPoints} نقطة لمّة",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                if (uiState.volunteer.wilaya.isNotBlank()) {
                                    Text(
                                        text = "${uiState.volunteer.neighborhood.ifBlank { uiState.volunteer.baladiya }} • ${uiState.volunteer.wilaya}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }

                // رسائل النجاح أو التنبيه
                AnimatedVisibility(visible = uiState.successMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.successMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // قسم الملف الشخصي والموقع
                Text(
                    text = "الملف الشخصي والموقع الجغرافي",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsActionItem(
                            icon = Icons.Default.Edit,
                            title = "تعديل الملف الشخصي (الاسم والصورة)",
                            subtitle = uiState.volunteer.name.ifBlank { "انقر لتعديل اسمك وصورتك الرمزية" },
                            onClick = { viewModel.openEditProfileDialog() },
                            testTag = "settings_edit_profile_item"
                        )
                        SettingsDivider()
                        SettingsActionItem(
                            icon = Icons.Default.LocationOn,
                            title = "تعديل الموقع (الحي/البلدية) يدويًا",
                            subtitle = if (uiState.volunteer.wilaya.isNotBlank()) "${uiState.volunteer.wilaya} • ${uiState.volunteer.baladiya} • ${uiState.volunteer.neighborhood}" else "انقر لتحديد وتعديل موقعك في الجزائر",
                            onClick = { viewModel.openLocationDialog() },
                            testTag = "settings_edit_location_item"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // قسم الإشعارات والتنبيهات
                Text(
                    text = "إدارة الإشعارات",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsToggleItem(
                            icon = Icons.Default.Notifications,
                            title = "إشعارات النداءات القريبة",
                            subtitle = "تنبيهك عند نشر طلب مساعدة جديد في حيك أو بلديتك",
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) },
                            testTag = "settings_notifications_toggle"
                        )
                        SettingsDivider()
                        SettingsToggleItem(
                            icon = Icons.Default.NotificationsActive,
                            title = "تنبيهات الحالات العاجلة فقط",
                            subtitle = "حصر الإشعارات اللحظية على النداءات الطارئة ذات الإلحاح المرتفع",
                            checked = uiState.urgentOnlyNotifications,
                            onCheckedChange = { viewModel.toggleUrgentOnly(it) },
                            testTag = "settings_urgent_toggle"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // قسم سياسة الخصوصية وحول التطبيق
                Text(
                    text = "الخصوصية وعن التطبيق",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsActionItem(
                            icon = Icons.Default.PrivacyTip,
                            title = "سياسة الخصوصية وحماية البيانات",
                            subtitle = "كيف نحمي بياناتك ورقم هاتفك وموقعك الجغرافي",
                            onClick = { viewModel.openPrivacyDialog() },
                            showArrow = true,
                            testTag = "settings_privacy_item"
                        )
                        SettingsDivider()
                        SettingsActionItem(
                            icon = Icons.Default.Info,
                            title = "حول تطبيق لمّة (Lamma)",
                            subtitle = "رؤية المنصة، ميثاق التكافل غير الربحي، والإصدار v1.0.0",
                            onClick = { viewModel.openAboutDialog() },
                            showArrow = true,
                            testTag = "settings_about_item"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // زر تسجيل الخروج أو تسجيل الدخول
                if (uiState.volunteer.phoneNumber.isNotBlank()) {
                    OutlinedButton(
                        onClick = { viewModel.openLogoutDialog() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("settings_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تسجيل الخروج من الحساب",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("settings_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تسجيل الدخول وتوثيق الحساب",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // نافذة تعديل الملف الشخصي (الاسم والصورة الرمزية)
        if (uiState.isEditProfileDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.closeEditProfileDialog() },
                title = { Text("تعديل الملف الشخصي", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "اختر صورتك الرمزية:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(volunteerAvatarOptions) { (emoji, label) ->
                                val isSelected = (uiState.selectedAvatar.ifBlank { "🤝" }) == emoji
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .clickable { viewModel.onAvatarSelected(emoji) }
                                        .then(
                                            if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                            else Modifier
                                        )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = emoji, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "الاسم أو الكنية التي تظهر لجيرانك:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.newNameInput,
                            onValueChange = { viewModel.onNewNameChanged(it) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_edit_name_textfield")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.saveProfile() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("settings_save_profile_button")
                    ) {
                        Text("حفظ التغييرات")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeEditProfileDialog() }) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // نافذة تعديل الموقع الجغرافي (الحي والبلدية والولاية يدويًا)
        if (uiState.isLocationDialogOpen) {
            LocationDialog(
                uiState = uiState,
                onWilayaSelect = { viewModel.selectWilaya(it) },
                onBaladiyaSelect = { viewModel.selectBaladiya(it) },
                onNeighborhoodChange = { viewModel.onNeighborhoodChanged(it) },
                onSave = { viewModel.saveLocation() },
                onDismiss = { viewModel.closeLocationDialog() }
            )
        }

        // نافذة تأكيد تسجيل الخروج
        if (uiState.isLogoutDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.closeLogoutDialog() },
                title = { Text("تأكيد تسجيل الخروج", fontWeight = FontWeight.Bold) },
                text = {
                    Text("هل أنت متأكد من رغبتك في تسجيل الخروج من حسابك في تطبيق لمّة؟ سيتعين عليك إدخال رقم هاتفك ورمز التحقق للعودة.")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmLogout() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("settings_confirm_logout_button")
                    ) {
                        Text("نعم، تسجيل الخروج")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeLogoutDialog() }) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // نافذة سياسة الخصوصية
        if (uiState.isPrivacyDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.closePrivacyDialog() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("سياسة الخصوصية وحماية البيانات", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "نحن في تطبيق \"لمّة\" نعتبر خصوصية وأمان المستخدمين أولويتنا القصوى:",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "1. رقم الهاتف: يُستخدم حصريًا للتحقق من الهوية عبر رمز التحقق (OTP) والتواصل المباشر مع أصحاب النداءات. لا يتم بيعه أو مشاركته مع أي جهة إعلانية أو تجارية.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "2. الموقع الجغرافي: يتم مشاركة موقع تقريبي على مستوى الحي والبلدية لحساب المسافة وعرض نداءات المساعدة القريبة دون كشف العنوان الدقيق منزلياً.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "3. العمل التطوعي الخالص: المنصة غير ربحية تمامًا ولا تطلب أي معلومات دفع أو بطاقات بنكية.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.closePrivacyDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("فهمت ذلك")
                    }
                }
            )
        }

        // نافذة حول تطبيق لمّة
        if (uiState.isAboutDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.closeAboutDialog() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("عن تطبيق لمّة (Lamma)", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "لمّة - تكافل مجتمعي في الوقت الفعلي",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تطبيق جزائري لربط المتطوعين وأبناء الحي بطلبات المساعدة اليومية العاجلة في الوقت الفعلي، مستلهماً قيم التويزة والتآخي بدون أي مقابل مادي.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "الإصدار: 1.0.0 (النسخة المجتمعية المستقرة)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.closeAboutDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("إغلاق")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showArrow: Boolean = true,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsDivider() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDialog(
    uiState: SettingsUiState,
    onWilayaSelect: (String) -> Unit,
    onBaladiyaSelect: (String) -> Unit,
    onNeighborhoodChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var wilayaExpanded by remember { mutableStateOf(false) }
    var baladiyaExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل الموقع (الحي والبلدية)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "حدد موقعك لتلقي وإرسال نداءات المساعدة القريبة في الوقت الفعلي:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // اختيار الولاية
                ExposedDropdownMenuBox(
                    expanded = wilayaExpanded,
                    onExpandedChange = { wilayaExpanded = !wilayaExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedWilaya,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الولاية") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wilayaExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = wilayaExpanded,
                        onDismissRequest = { wilayaExpanded = false }
                    ) {
                        uiState.availableWilayas.forEach { w ->
                            DropdownMenuItem(
                                text = { Text(w) },
                                onClick = {
                                    onWilayaSelect(w)
                                    wilayaExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // اختيار البلدية
                ExposedDropdownMenuBox(
                    expanded = baladiyaExpanded,
                    onExpandedChange = { baladiyaExpanded = !baladiyaExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedBaladiya,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("البلدية") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = baladiyaExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = baladiyaExpanded,
                        onDismissRequest = { baladiyaExpanded = false }
                    ) {
                        uiState.availableBaladiyas.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b) },
                                onClick = {
                                    onBaladiyaSelect(b)
                                    baladiyaExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // كتابة الحي يدويًا
                OutlinedTextField(
                    value = uiState.selectedNeighborhood,
                    onValueChange = onNeighborhoodChange,
                    label = { Text("الحي أو التجمع السكني (يدويًا)") },
                    placeholder = { Text("مثال: حي النصر، وسط المدينة، حي 500 مسكن") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("settings_save_location_button")
            ) {
                Text("حفظ الموقع")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
