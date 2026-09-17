package com.example.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.HelpCall
import com.example.data.model.HelpCallStatus
import com.example.data.model.UrgencyLevel
import com.example.ui.feed.UrgencyBadge
import com.example.ui.feed.getCategoryIcon
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * توليد نقطة حمراء واضحة (Marker) بموقع نشر نداء المساعدة
 * تصميم واضح ومميز يراه المتطوعون فوراً على الخريطة:
 * - عاجل: أحمر نابض وساطع مع هالة إشعاعية مضيئة
 * - عادي / منخفض: برتقالي / كهرماني دافئ
 * - تم إنجازه: رمادي هادئ
 */
fun createRedNeedMarker(
    context: Context,
    urgency: UrgencyLevel,
    status: HelpCallStatus = HelpCallStatus.OPEN,
    isSelected: Boolean = false
): Drawable {
    val baseSize = if (isSelected) 104 else 86
    val height = (baseSize * 1.25f).toInt()
    val bitmap = Bitmap.createBitmap(baseSize, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val centerX = baseSize / 2f
    val centerY = baseSize / 2.3f
    val radius = baseSize * 0.36f

    val isUrgent = urgency == UrgencyLevel.URGENT
    val isCompleted = status == HelpCallStatus.COMPLETED

    val mainColor = when {
        isCompleted -> AndroidColor.rgb(156, 163, 175) // رمادي للنداء المكتمل
        status == HelpCallStatus.IN_PROGRESS -> AndroidColor.rgb(217, 119, 6) // كهرماني قيد التنفيذ
        isUrgent -> AndroidColor.rgb(225, 29, 72) // أحمر ساطع ونابض للنداءات العاجلة
        urgency == UrgencyLevel.MEDIUM -> AndroidColor.rgb(234, 88, 12) // برتقالي دافئ
        else -> AndroidColor.rgb(202, 138, 4) // أصفر / كهرماني
    }

    // 1. هالة خارجية مضيئة شبه شفافة (نبض ضوئي)
    val haloAlpha = when {
        isCompleted -> 40
        isSelected -> 140
        isUrgent -> 110
        else -> 75
    }
    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mainColor
        alpha = haloAlpha
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, if (isUrgent) radius * 1.35f else radius * 1.22f, haloPaint)

    // 2. سهم المؤشر السفلي المتصل بالدائرة
    val pinPath = Path().apply {
        moveTo(centerX - radius * 0.65f, centerY + radius * 0.35f)
        lineTo(centerX, height.toFloat() - 2f)
        lineTo(centerX + radius * 0.65f, centerY + radius * 0.35f)
        close()
    }
    val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mainColor
        style = Paint.Style.FILL
    }
    canvas.drawPath(pinPath, pointerPaint)

    // 3. النواة الرئيسية: دائرة ملونة حسب الإلحاح والحالة
    val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mainColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius, corePaint)

    // 4. إطار أبيض ناصع يبرز النقطة على خلفية أي خريطة
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = baseSize * 0.075f
    }
    canvas.drawCircle(centerX, centerY, radius - (baseSize * 0.035f), ringPaint)

    // 5. النقطة البيضاء المركزية
    val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius * 0.35f, centerDotPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * علامة موقع المتطوع الحالي على الخريطة
 */
fun createUserLocationMarker(context: Context): Drawable {
    val size = 64
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val center = size / 2f

    // هالة فيروزية
    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.argb(80, 15, 118, 110)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, center * 0.95f, outerPaint)

    // حلقة بيضاء
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, center * 0.55f, ringPaint)

    // نقطة فيروزية وسطية
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(15, 118, 110)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, center * 0.40f, innerPaint)

    return BitmapDrawable(context.resources, bitmap)
}

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.5)
            val centerPoint = GeoPoint(uiState.centerLatitude, uiState.centerLongitude)
            controller.setCenter(centerPoint)
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = modifier.fillMaxSize()) {
            // خريطة OpenStreetMap عبر osmdroid
            AndroidView(
                factory = { mapView },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("osm_map_view"),
                update = { view ->
                    view.overlays.clear()

                    // 1. علامة موقع المستخدم الحالي
                    if (uiState.centerLatitude != 0.0 && uiState.centerLongitude != 0.0) {
                        val userMarker = Marker(view).apply {
                            position = GeoPoint(uiState.centerLatitude, uiState.centerLongitude)
                            title = "موقعي الحالي"
                            icon = createUserLocationMarker(context)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        }
                        view.overlays.add(userMarker)
                    }

                    // 2. نقاط النداءات الحمراء البارزة (Markers)
                    uiState.calls.forEach { call ->
                        if (call.latitude != 0.0 && call.longitude != 0.0) {
                            val isSelected = uiState.selectedCall?.id == call.id
                            val marker = Marker(view).apply {
                                position = GeoPoint(call.latitude, call.longitude)
                                title = call.title
                                subDescription = "${call.category.labelArabic} • ${call.locationName}"
                                icon = createRedNeedMarker(
                                    context = context,
                                    urgency = call.urgency,
                                    status = call.status,
                                    isSelected = isSelected
                                )
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                setOnMarkerClickListener { _, _ ->
                                    viewModel.selectCall(call)
                                    true
                                }
                            }
                            view.overlays.add(marker)
                        }
                    }
                    view.invalidate()
                }
            )

            // زر إعادة التمركز حول الموقع الحالي
            FloatingActionButton(
                onClick = {
                    mapView.controller.animateTo(
                        GeoPoint(uiState.centerLatitude, uiState.centerLongitude)
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .testTag("recenter_map_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "موقعي الحالي"
                )
            }

            // بطاقة معاينة النداء المحدد أسفل الشاشة
            AnimatedVisibility(
                visible = uiState.selectedCall != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                uiState.selectedCall?.let { call ->
                    SelectedCallMapCard(
                        call = call,
                        userLat = uiState.centerLatitude,
                        userLon = uiState.centerLongitude,
                        onVolunteer = { viewModel.volunteerForSelectedCall() },
                        onDismiss = { viewModel.selectCall(null) },
                        isActionLoading = uiState.isActionInProgress
                    )
                }
            }

            // إشعار الإنجاز
            uiState.noticeMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    action = {
                        IconButton(onClick = { viewModel.clearNotice() }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    }
}

@Composable
fun SelectedCallMapCard(
    call: HelpCall,
    userLat: Double,
    userLon: Double,
    onVolunteer: () -> Unit,
    onDismiss: () -> Unit,
    isActionLoading: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_call_map_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // أيقونة الفئة
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(call.category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = call.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "📍 ${call.locationName.ifBlank { "موقع النداء" }} (${call.formatDistance(userLat, userLon)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = call.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UrgencyBadge(urgency = call.urgency)
                Spacer(modifier = Modifier.weight(1f))

                if (call.status == HelpCallStatus.OPEN) {
                    Button(
                        onClick = onVolunteer,
                        enabled = !isActionLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("map_volunteer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("أنا سأساعد", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = call.status.labelArabic,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
