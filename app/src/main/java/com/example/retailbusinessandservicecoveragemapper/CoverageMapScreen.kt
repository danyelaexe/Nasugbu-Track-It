package com.example.retailbusinessandservicecoveragemapper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.camera.CameraPosition as MapLibreCameraPosition
import org.maplibre.android.camera.CameraUpdateFactory as MapLibreCameraUpdateFactory
import org.maplibre.android.geometry.LatLng as MapLibreLatLng
import org.maplibre.android.maps.MapView as MapLibreMapView
import org.maplibre.android.maps.Style
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CoverageMapScreen(
    branches: List<Branch>,
    focusedBranch: Branch?,
    onClearFocus: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val nasugbuBoundary = remember { loadNasugbuBoundary(context) }
    var activeBoundaryPoints by remember { mutableStateOf(nasugbuBoundary) }
    var selectedBarangay by remember { mutableStateOf("Nasugbu Center (Poblacion)") }
    var selectedBranchOnMap by remember { mutableStateOf(focusedBranch) }
    var currentStyle by remember { mutableStateOf("liberty") }

    val mapLibreMapState = remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }

    val centerLat = focusedBranch?.latitude ?: NASUGBU_CENTER.latitude
    val centerLng = focusedBranch?.longitude ?: NASUGBU_CENTER.longitude
    val zoom = if (focusedBranch != null) 15.0 else 12.0

    val mapView = remember {
        MapLibre.getInstance(context)
        MapLibreMapView(context).apply {
            onCreate(Bundle())
        }
    }

    DisposableEffect(mapView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            mapLibreMapState.value?.let { map ->
                map.style?.let { style ->
                    enableLocationComponent(style, map, context)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    getMapAsync { map ->
                        mapLibreMapState.value = map

                        map.uiSettings.apply {
                            isZoomGesturesEnabled = true
                            isScrollGesturesEnabled = true
                            isRotateGesturesEnabled = true
                            isTiltGesturesEnabled = true
                            isDoubleTapGesturesEnabled = true
                        }

                        val styleUrl = "https://tiles.openfreemap.org/styles/$currentStyle"
                        map.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                enableLocationComponent(style, map, context)
                            }
                        }

                        map.cameraPosition = MapLibreCameraPosition.Builder()
                            .target(MapLibreLatLng(centerLat, centerLng))
                            .zoom(zoom)
                            .build()

                        if (activeBoundaryPoints.isNotEmpty()) {
                            @Suppress("DEPRECATION")
                            val polyOptions = PolygonOptions().apply {
                                addAll(activeBoundaryPoints.map { MapLibreLatLng(it.latitude, it.longitude) })
                                fillColor(android.graphics.Color.argb(50, 45, 120, 84))
                                strokeColor(android.graphics.Color.parseColor("#1B5E20"))
                            }
                            map.addPolygon(polyOptions)
                        }

                        branches.forEach { branch ->
                            @Suppress("DEPRECATION")
                            val markerOptions = MarkerOptions().apply {
                                position(MapLibreLatLng(branch.latitude, branch.longitude))
                                title(branch.name)
                                snippet("${branch.category} • ${branch.address}")
                            }
                            map.addMarker(markerOptions)
                        }

                        map.setOnMarkerClickListener { marker ->
                            val clickedBranch = branches.find { it.name == marker.title }
                            if (clickedBranch != null) {
                                selectedBranchOnMap = clickedBranch
                            }
                            false
                        }
                    }
                }
            },
            update = {
                mapLibreMapState.value?.let { map ->
                    map.animateCamera(
                        MapLibreCameraUpdateFactory.newLatLngZoom(MapLibreLatLng(centerLat, centerLng), zoom)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "🗺️ Nasugbu OpenFreeMap",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Active Points: ${activeBoundaryPoints.size} boundary nodes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    TextButton(onClick = {
                                        currentStyle = "liberty"
                                        mapLibreMapState.value?.setStyle("https://tiles.openfreemap.org/styles/liberty")
                                    }) {
                                        Text("Liberty", fontWeight = if (currentStyle == "liberty") FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.labelSmall)
                                    }
                                    TextButton(onClick = {
                                        currentStyle = "bright"
                                        mapLibreMapState.value?.setStyle("https://tiles.openfreemap.org/styles/bright")
                                    }) {
                                        Text("Bright", fontWeight = if (currentStyle == "bright") FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.labelSmall)
                                    }
                                    TextButton(onClick = {
                                        currentStyle = "positron"
                                        mapLibreMapState.value?.setStyle("https://tiles.openfreemap.org/styles/positron")
                                    }) {
                                        Text("Positron", fontWeight = if (currentStyle == "positron") FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text("📍 Quick Jump to Barangay in Nasugbu:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(NasugbuBoundary.NASUGBU_BARANGAY_LOCATIONS.keys.toList()) { barangayName ->
                            val barangayCoords = NasugbuBoundary.NASUGBU_BARANGAY_LOCATIONS[barangayName]
                            FilterChip(
                                selected = selectedBarangay == barangayName,
                                onClick = {
                                    selectedBarangay = barangayName
                                    barangayCoords?.let { targetLatLng ->
                                        mapLibreMapState.value?.animateCamera(
                                            MapLibreCameraUpdateFactory.newLatLngZoom(MapLibreLatLng(targetLatLng.latitude, targetLatLng.longitude), 15.0)
                                        )
                                    }
                                },
                                label = { Text(barangayName, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (selectedBranchOnMap != null) 270.dp else 84.dp, end = 16.dp)
                .clickable {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        mapLibreMapState.value?.let { map ->
                            val userLoc = map.locationComponent.lastKnownLocation
                            if (userLoc != null) {
                                map.animateCamera(
                                    MapLibreCameraUpdateFactory.newLatLngZoom(
                                        MapLibreLatLng(userLoc.latitude, userLoc.longitude),
                                        16.0
                                    )
                                )
                            } else {
                                map.animateCamera(
                                    MapLibreCameraUpdateFactory.newLatLngZoom(
                                        MapLibreLatLng(NASUGBU_CENTER.latitude, NASUGBU_CENTER.longitude),
                                        14.0
                                    )
                                )
                            }
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📍", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(6.dp))
                Text("My Location", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (selectedBranchOnMap != null) 210.dp else 24.dp, end = 16.dp)
                .clickable {
                    mapLibreMapState.value?.animateCamera(
                        MapLibreCameraUpdateFactory.newLatLngZoom(MapLibreLatLng(NASUGBU_CENTER.latitude, NASUGBU_CENTER.longitude), 12.0)
                    )
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎯", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(6.dp))
                Text("Nasugbu Center", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = if (selectedBranchOnMap != null) 210.dp else 24.dp, start = 16.dp),
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shadowElevation = 2.dp
        ) {
            Text(
                "© OpenFreeMap & OpenStreetMap contributors",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        selectedBranchOnMap?.let { branch ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            branch.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                selectedBranchOnMap = null
                                onClearFocus()
                            }
                        ) {
                            Text("Close")
                        }
                    }
                    Text("Category: ${branch.category}", style = MaterialTheme.typography.bodyMedium)
                    Text("Address: ${branch.address}", style = MaterialTheme.typography.bodyMedium)
                    if (branch.contact.isNotBlank()) {
                        Text("Contact: ${branch.contact}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        "Coverage Radius: ${branch.coverageRadiusKm} km | Lat: ${branch.latitude}, Lng: ${branch.longitude}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            mapLibreMapState.value?.animateCamera(
                                MapLibreCameraUpdateFactory.newLatLngZoom(MapLibreLatLng(branch.latitude, branch.longitude), 16.0)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Center Map on ${branch.name}")
                    }
                }
            }
        }
    }
}

private fun loadNasugbuBoundary(context: Context): List<com.google.android.gms.maps.model.LatLng> {
    try {
        val json = context.assets
            .open("nasugbu.geojson")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(json)
        val features = root.optJSONArray("features")
        if (features != null && features.length() > 0) {
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val geometry = feature.optJSONObject("geometry")
                if (geometry != null) {
                    val type = geometry.optString("type")
                    val coordinates = geometry.optJSONArray("coordinates")
                    if (coordinates != null) {
                        if (type.equals("Polygon", ignoreCase = true)) {
                            val outerRing = coordinates.optJSONArray(0)
                            if (outerRing != null && outerRing.length() > 0) {
                                val points = mutableListOf<com.google.android.gms.maps.model.LatLng>()
                                for (j in 0 until outerRing.length()) {
                                    val pt = outerRing.optJSONArray(j)
                                    if (pt != null && pt.length() >= 2) {
                                        points.add(com.google.android.gms.maps.model.LatLng(pt.getDouble(1), pt.getDouble(0)))
                                    }
                                }
                                if (points.isNotEmpty()) return points
                            }
                        } else if (type.equals("MultiPolygon", ignoreCase = true)) {
                            val poly = coordinates.optJSONArray(0)
                            if (poly != null) {
                                val outerRing = poly.optJSONArray(0)
                                if (outerRing != null && outerRing.length() > 0) {
                                    val points = mutableListOf<com.google.android.gms.maps.model.LatLng>()
                                    for (j in 0 until outerRing.length()) {
                                        val pt = outerRing.optJSONArray(j)
                                        if (pt != null && pt.length() >= 2) {
                                            points.add(com.google.android.gms.maps.model.LatLng(pt.getDouble(1), pt.getDouble(0)))
                                        }
                                    }
                                    if (points.isNotEmpty()) return points
                                }
                            }
                        }
                    }
                }
            }
        }
    } catch (_: Exception) {
        // Fallback to default points if asset parsing fails
    }
    return NasugbuBoundary.DEFAULT_POINTS
}

private fun enableLocationComponent(style: Style, map: org.maplibre.android.maps.MapLibreMap, context: Context) {
    try {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            val locationComponent = map.locationComponent
            val options = org.maplibre.android.location.LocationComponentActivationOptions.builder(context, style)
                .useDefaultLocationEngine(true)
                .build()
            locationComponent.activateLocationComponent(options)
            locationComponent.isLocationComponentEnabled = true
            locationComponent.renderMode = org.maplibre.android.location.modes.RenderMode.COMPASS
            locationComponent.cameraMode = org.maplibre.android.location.modes.CameraMode.TRACKING
        }
    } catch (_: Exception) {}
}
