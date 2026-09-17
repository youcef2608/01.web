package com.example.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Badge
import com.example.data.model.Volunteer
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
    onOpenLogin: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(bottom = 96.dp)
        ) {
            // ترويسة الملف الشخصي
            ProfileHeaderCard(
                volunteer = uiState.volunteer,
                onOpenAuth = onOpenLogin,
                onOpenSettings = onOpenSettings
            )

            Spacer(modifier = Modifier.height(16.dp))

            // شجرة الأثر التكافلي التفاعلية (Canvas)
            ImpactTreeSection(volunteer = uiState.volunteer)

            Spacer(modifier = Modifier.height(16.dp))

            // عداد التطوع العكسي (معطي مقابل مستفيد)
            ReciprocalVolunteeringCounter(volunteer = uiState.volunteer)

            Spacer(modifier = Modifier.height(16.dp))

            // قائمة الشارات التقديرية
            BadgesSection(badges = uiState.volunteer.badges)

            Spacer(modifier = Modifier.height(16.dp))

            // سجل قصص الأثر المولدة بواسطة Gemini
            if (uiState.echoHistory.isNotEmpty()) {
                EchoHistorySection(uiState = uiState)
            }
        }

        // نافذة تسجيل الدخول برقم الهاتف و OTP
        if (uiState.isLoginDialogVisible) {
            PhoneAuthDialog(
                uiState = uiState,
                onPhoneChange = { viewModel.onPhoneChange(it) },
                onNameChange = { viewModel.onNameChange(it) },
                onOtpChange = { viewModel.onOtpChange(it) },
                onRequestOtp = { viewModel.requestOtp() },
                onVerifyOtp = { viewModel.verifyOtpAndLogin() },
                onDismiss = { viewModel.hideLoginDialog() }
            )
        }
    }
}

@Composable
fun ProfileHeaderCard(
    volunteer: Volunteer,
    onOpenAuth: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = volunteer.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (volunteer.wilaya.isNotBlank()) "${volunteer.neighborhood} • ${volunteer.wilaya}" else "حدد موقعك في الإعدادات",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (volunteer.phoneNumber.isNotBlank()) volunteer.phoneNumber else "لم يسجل الهاتف بعد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onOpenAuth,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("open_auth_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (volunteer.phoneNumber.isNotBlank()) "الحساب" else "دخول")
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("profile_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // رصيد الأصداء وسلسلة الأثر
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${volunteer.echoPoints}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "نقاط لمّة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${volunteer.impactStreak}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "سلسلة الأثر المتصل",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

/**
 * شجرة الأثر التكافلي (Canvas في Compose)
 * تكبر بصرياً بإضافة فروع وأوراق وأمواج صدى مع كل عملية تطوع
 */
@Composable
fun ImpactTreeSection(volunteer: Volunteer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "شجرة وسلسلة الأثر التكافلي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "تنمو فروع الشجرة وتزهر مع كل عمل خيري تقدمه في حيك.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            // رسم الشجرة بالـ Canvas
            val treeColor = MaterialTheme.colorScheme.primary
            val leafColor = MaterialTheme.colorScheme.tertiary
            val blossomColor = MaterialTheme.colorScheme.secondary
            val givenCount = volunteer.givenHelpsCount

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("impact_tree_canvas")
            ) {
                val width = size.width
                val height = size.height
                val rootX = width / 2
                val rootY = height - 20f

                // الجذع الأساسي
                drawLine(
                    color = treeColor,
                    start = Offset(rootX, rootY),
                    end = Offset(rootX, rootY - 60f),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )

                // حلقات صدى الأثر حول قمة الجذع
                val echoRadius = 20f + (givenCount * 4f).coerceAtMost(55f)
                drawCircle(
                    color = blossomColor.copy(alpha = 0.25f),
                    radius = echoRadius,
                    center = Offset(rootX, rootY - 65f),
                    style = Stroke(width = 2.5f)
                )

                // الفروع والأوراق بناءً على عدد مرات التطوع (givenHelpsCount)
                val branchesCount = (givenCount + 3).coerceIn(3, 14)
                for (i in 0 until branchesCount) {
                    val angleDeg = 190.0 + (i.toDouble() / (branchesCount - 1)) * 160.0
                    val angleRad = Math.toRadians(angleDeg)
                    val branchLength = 35f + ((i * 7) % 30)

                    val startBranch = Offset(rootX, rootY - 60f)
                    val endBranch = Offset(
                        (rootX + cos(angleRad) * branchLength).toFloat(),
                        (rootY - 60f + sin(angleRad) * branchLength).toFloat()
                    )

                    drawLine(
                        color = treeColor.copy(alpha = 0.85f),
                        start = startBranch,
                        end = endBranch,
                        strokeWidth = 4.5f,
                        cap = StrokeCap.Round
                    )

                    // أوراق الشجرة / أزهار صدى الخير
                    drawCircle(
                        color = if (i % 2 == 0) leafColor else blossomColor,
                        radius = 6.5f,
                        center = endBranch
                    )
                }

                // زهرة المركز الكبرى (نواة الصدى)
                drawCircle(
                    color = blossomColor,
                    radius = 9f,
                    center = Offset(rootX, rootY - 65f)
                )
            }
        }
    }
}

/**
 * عداد التطوع العكسي: عدد المرات التي كان فيها المستخدم معطي مقابل مستفيد
 */
@Composable
fun ReciprocalVolunteeringCounter(volunteer: Volunteer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Balance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ميزان التطوع العكسي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "في لمّة لا توجد فوارق: الجميع يُعطي حين يستطيع، ويطلب العون حين يحتاج.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // جهة العطاء
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${volunteer.givenHelpsCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "مرات كُنتَ معطياً",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // شريط التوازن النسبي
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val total = (volunteer.givenHelpsCount + volunteer.receivedHelpsCount).coerceAtLeast(1)
                    val ratio = (volunteer.givenHelpsCount.toFloat() / total.toFloat())

                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تكافل متوازن ${(ratio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // جهة الاستفادة
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${volunteer.receivedHelpsCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "مرات كُنتَ مستفيداً",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BadgesSection(badges: List<Badge>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "الشارات التقديرية المكتسبة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            badges.forEach { badge ->
                BadgeRow(badge = badge)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BadgeRow(badge: Badge) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (badge.isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getBadgeIcon(badge.iconName),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = badge.titleArabic,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (badge.isUnlocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "مكتملة",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = badge.descriptionArabic,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (!badge.isUnlocked) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { badge.progress.toFloat() / badge.maxProgress.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EchoHistorySection(uiState: ProfileUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "قصص الأثر التكافلي المسجلة (Gemini)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.echoHistory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لم يتم تسجيل أي أثر تكافلي بعد. بادر بالخير!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                uiState.echoHistory.forEach { echo ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = echo.generatedStory,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "+${echo.echoPointsAwarded} صدى تكافلي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getBadgeIcon(name: String): ImageVector = when (name) {
    "Shield" -> Icons.Default.Shield
    "EmojiEvents" -> Icons.Default.EmojiEvents
    "Favorite" -> Icons.Default.Favorite
    "Balance" -> Icons.Default.Balance
    else -> Icons.Default.VolunteerActivism
}

@Composable
fun PhoneAuthDialog(
    uiState: ProfileUiState,
    onPhoneChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onRequestOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل الدخول برقم الهاتف",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "مصادقة سريعة وآمنة عبر رمز OTP بدون كلمة مرور وبدون أي بيانات بنكية.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!uiState.isOtpSent) {
                    OutlinedTextField(
                        value = uiState.nameInput,
                        onValueChange = onNameChange,
                        label = { Text("اسمك الكامل") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.phoneNumberInput,
                        onValueChange = onPhoneChange,
                        label = { Text("رقم الهاتف (مع الرمز الدولي)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.otpInput,
                        onValueChange = onOtpChange,
                        label = { Text("رمز التحقق (OTP)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("1234") }
                    )
                }

                uiState.authError?.let { err ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            if (!uiState.isOtpSent) {
                Button(
                    onClick = onRequestOtp,
                    enabled = !uiState.isAuthLoading
                ) {
                    Text("إرسال رمز التحقق (SMS)")
                }
            } else {
                Button(
                    onClick = onVerifyOtp,
                    enabled = !uiState.isAuthLoading
                ) {
                    Text("تأكيد الدخول")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
