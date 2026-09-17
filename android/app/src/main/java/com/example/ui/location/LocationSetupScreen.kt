package com.example.ui.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LocationSetupScreen(
    viewModel: LocationSetupViewModel,
    onLocationSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    var wilayaExpanded by remember { mutableStateOf(false) }
    var baladiyaExpanded by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "أين تتواجد للعمل التطوعي؟",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "لتخصيص نداءات المساعدة وصدارة المتطوعين في منطقتك، يرجى تحديد موقعك. (لن نقوم بتخزين موقعك الدقيق للخصوصية).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Auto Detect Button
            OutlinedButton(
                onClick = {
                    if (locationPermissionState.status.isGranted) {
                        viewModel.autoDetectLocation()
                    } else {
                        locationPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isAutoDetecting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تحديد موقعي تلقائياً")
                }
            }

            uiState.autoDetectError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wilaya Dropdown
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
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = wilayaExpanded,
                    onDismissRequest = { wilayaExpanded = false }
                ) {
                    uiState.wilayas.forEach { wilaya ->
                        DropdownMenuItem(
                            text = { Text(wilaya) },
                            onClick = {
                                viewModel.onWilayaSelected(wilaya)
                                wilayaExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Baladiya Dropdown
            val availableBaladiyas = uiState.baladiyas[uiState.selectedWilaya] ?: emptyList()
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
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, uiState.selectedWilaya.isNotBlank()),
                    enabled = uiState.selectedWilaya.isNotBlank()
                )
                ExposedDropdownMenu(
                    expanded = baladiyaExpanded,
                    onDismissRequest = { baladiyaExpanded = false }
                ) {
                    availableBaladiyas.forEach { baladiya ->
                        DropdownMenuItem(
                            text = { Text(baladiya) },
                            onClick = {
                                viewModel.onBaladiyaSelected(baladiya)
                                baladiyaExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Neighborhood Text Field
            OutlinedTextField(
                value = uiState.neighborhood,
                onValueChange = { viewModel.onNeighborhoodChanged(it) },
                label = { Text("الحي / الشارع (اختياري)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.saveLocation()
                    onLocationSaved()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedWilaya.isNotBlank() && uiState.selectedBaladiya.isNotBlank()
            ) {
                Text(
                    text = "تأكيد والانطلاق",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
