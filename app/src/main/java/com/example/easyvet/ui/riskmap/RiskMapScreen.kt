package com.example.easyvet.ui.riskmap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyvet.data.local.entity.OutbreakAlertEntity
import com.example.easyvet.data.model.AlertSeverity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RiskMapScreen(
    viewModel: RiskMapViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToAdvisory: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Geospatial Risk Map",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.totalActiveOutbreaks} Active Outbreaks • ${uiState.highRiskZonesCount} High Risk Zones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleMapViewMode() }) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Toggle Map Mode",
                            tint = if (uiState.mapViewMode == MapViewMode.QUARANTINE_PERIMETERS) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Quick Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by disease, region, or alert title...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Species Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val speciesList = listOf("All", "Cattle", "Goat", "Sheep", "Pig", "Buffalo")
                    items(speciesList) { species ->
                        FilterChip(
                            selected = uiState.selectedSpeciesFilter == species,
                            onClick = { viewModel.setSpeciesFilter(species) },
                            label = { Text(species) }
                        )
                    }
                }
            }

            // Key Metrics Summary Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    label = "Active Cases",
                    value = uiState.totalActiveCases.toString(),
                    color = Color(0xFFE65100),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "Mortalities",
                    value = uiState.totalMortalities.toString(),
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "View Mode",
                    value = if (uiState.mapViewMode == MapViewMode.HOTSPOTS) "Hotspots" else "Quarantine",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.2f)
                )
            }

            // Interactive Geospatial Risk Map Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                GeospatialRiskCanvas(
                    alerts = uiState.filteredAlerts,
                    selectedAlert = uiState.selectedAlert,
                    mapViewMode = uiState.mapViewMode,
                    onAlertSelected = { alert -> viewModel.selectAlert(alert) },
                    modifier = Modifier.fillMaxSize()
                )

                // Map Legend Overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendDot(color = Color(0xFFD32F2F), label = "Critical")
                        LegendDot(color = Color(0xFFE65100), label = "High")
                        LegendDot(color = Color(0xFFF57F17), label = "Warning")
                    }
                }
            }

            // List of Active Outbreak Alerts
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Outbreak Alerts & Corridors (${uiState.filteredAlerts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.selectedAlert != null) {
                            Text(
                                text = "Clear Selection",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { viewModel.selectAlert(null) }
                            )
                        }
                    }
                }

                if (uiState.filteredAlerts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No outbreak alerts match the current filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(uiState.filteredAlerts, key = { it.id }) { alert ->
                    OutbreakAlertCard(
                        alert = alert,
                        isSelected = uiState.selectedAlert?.id == alert.id,
                        onClick = { viewModel.selectAlert(alert) },
                        onViewAdvisory = { onNavigateToAdvisory(alert.diseaseName) }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet when an alert is selected
    if (uiState.selectedAlert != null) {
        val sheetState = rememberModalBottomSheetState()
        val alert = uiState.selectedAlert!!

        ModalBottomSheet(
            onDismissRequest = { viewModel.selectAlert(null) },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeverityBadge(severity = alert.severity)
                    Text(
                        text = "Risk Score: ${alert.riskScore}/100",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (alert.riskScore >= 70) Color(0xFFD32F2F) else Color(0xFFE65100)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = alert.regionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active Cases", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${alert.activeCases}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Mortality", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${alert.mortalityCount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Buffer Radius", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${alert.radiusKm.toInt()} km",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Quarantine & Biosecurity Advisory",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.advisory,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.selectAlert(null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = {
                            val disease = alert.diseaseName
                            viewModel.selectAlert(null)
                            onNavigateToAdvisory(disease)
                        },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Guidelines")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun GeospatialRiskCanvas(
    alerts: List<OutbreakAlertEntity>,
    selectedAlert: OutbreakAlertEntity?,
    mapViewMode: MapViewMode,
    onAlertSelected: (OutbreakAlertEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val corridorColor = Color(0xFF00897B)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(alerts) {
                detectTapGestures { tapOffset ->
                    val width = size.width
                    val height = size.height

                    // Find closest alert point
                    val matchedAlert = alerts.minByOrNull { alert ->
                        val point = computePoint(alert, width.toFloat(), height.toFloat())
                        val dx = tapOffset.x - point.x
                        val dy = tapOffset.y - point.y
                        dx * dx + dy * dy
                    }

                    if (matchedAlert != null) {
                        val point = computePoint(matchedAlert, width.toFloat(), height.toFloat())
                        val dx = tapOffset.x - point.x
                        val dy = tapOffset.y - point.y
                        if (dx * dx + dy * dy <= 80 * 80) { // tap threshold
                            onAlertSelected(matchedAlert)
                        }
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        // 1. Draw Grid Lines (Latitude/Longitude mesh)
        val cols = 6
        val rows = 4
        for (i in 1 until cols) {
            val x = (width / cols) * i
            drawLine(
                color = gridLineColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }
        for (i in 1 until rows) {
            val y = (height / rows) * i
            drawLine(
                color = gridLineColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // 2. Draw Vector Risk Corridors (connecting nearby active outbreak nodes)
        if (mapViewMode == MapViewMode.QUARANTINE_PERIMETERS && alerts.size >= 2) {
            val points = alerts.map { computePoint(it, width, height) }
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = corridorColor.copy(alpha = 0.5f),
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                )
            }
        }

        // 3. Draw Outbreak Nodes & Quarantine Radii
        alerts.forEach { alert ->
            val center = computePoint(alert, width, height)
            val isSelected = selectedAlert?.id == alert.id

            val baseColor = when (alert.severity) {
                AlertSeverity.CRITICAL -> Color(0xFFD32F2F)
                AlertSeverity.HIGH -> Color(0xFFE65100)
                AlertSeverity.WARNING -> Color(0xFFF57F17)
                AlertSeverity.INFO -> Color(0xFF1976D2)
            }

            val radiusPx = (alert.radiusKm.toFloat() * 2.2f).coerceIn(30f, 110f)

            // Quarantine Zone Radius Circle
            drawCircle(
                color = baseColor.copy(alpha = if (isSelected) pulseAlpha + 0.1f else 0.18f),
                radius = if (isSelected) radiusPx * 1.25f else radiusPx,
                center = center
            )
            drawCircle(
                color = baseColor.copy(alpha = 0.6f),
                radius = if (isSelected) radiusPx * 1.25f else radiusPx,
                center = center,
                style = Stroke(
                    width = if (isSelected) 3f else 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            )

            // Center Pin Node
            val pinRadius = if (isSelected) 18f else 12f
            drawCircle(
                color = Color.White,
                radius = pinRadius + 4f,
                center = center
            )
            drawCircle(
                color = baseColor,
                radius = pinRadius,
                center = center
            )
        }
    }
}

private fun computePoint(alert: OutbreakAlertEntity, width: Float, height: Float): Offset {
    // Standard mapping of lat/lon bounding box for region projection
    // Lat range roughly -7.5 to -2.0, Lon range roughly 32.0 to 40.0
    val minLat = -8.0
    val maxLat = -1.5
    val minLon = 31.5
    val maxLon = 40.0

    val xFrac = ((alert.longitude - minLon) / (maxLon - minLon)).coerceIn(0.1, 0.9).toFloat()
    val yFrac = (1.0 - ((alert.latitude - minLat) / (maxLat - minLat))).coerceIn(0.1, 0.9).toFloat()

    return Offset(xFrac * width, yFrac * height)
}

@Composable
private fun OutbreakAlertCard(
    alert: OutbreakAlertEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onViewAdvisory: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeverityBadge(severity = alert.severity)

                Text(
                    text = "${alert.radiusKm.toInt()} km Quarantine Radius",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${alert.regionName} • ${alert.diseaseName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Risk Score & Case Counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Epidemiological Risk",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "${alert.riskScore}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (alert.riskScore >= 70) Color(0xFFD32F2F) else Color(0xFFE65100)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { alert.riskScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (alert.riskScore >= 70) Color(0xFFD32F2F) else Color(0xFFE65100),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${alert.activeCases} Active Cases",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = "${alert.mortalityCount} Deaths",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = alert.advisory,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                AssistChip(
                    onClick = onViewAdvisory,
                    label = { Text("View Advisory Protocol") },
                    leadingIcon = {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
private fun SeverityBadge(severity: AlertSeverity) {
    val (bgColor, textColor, label) = when (severity) {
        AlertSeverity.CRITICAL -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "CRITICAL OUTBREAK")
        AlertSeverity.HIGH -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "HIGH RISK ZONE")
        AlertSeverity.WARNING -> Triple(Color(0xFFFFFDE7), Color(0xFFF57F17), "WATCH WARNING")
        AlertSeverity.INFO -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "INFO NOTICE")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 10.sp
        )
    }
}
