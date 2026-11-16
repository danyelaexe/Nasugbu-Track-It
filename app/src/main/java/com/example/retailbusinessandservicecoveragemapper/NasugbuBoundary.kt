package com.example.retailbusinessandservicecoveragemapper

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import org.json.JSONArray
import org.json.JSONObject

val NASUGBU_CENTER = LatLng(14.0732603, 120.6326531)

object NasugbuBoundary {
    val NASUGBU_BARANGAYS = listOf(
        "Poblacion Ward 1", "Poblacion Ward 2", "Poblacion Ward 3", "Poblacion Ward 4",
        "Poblacion Ward 5", "Poblacion Ward 6", "Poblacion Ward 7", "Poblacion Ward 8",
        "Poblacion Ward 9", "Poblacion Ward 10", "Poblacion Ward 11", "Poblacion Ward 12",
        "Aga", "Balaytigue", "Banilad", "Bilaran", "Bucana", "Calayo", "Catandaan",
        "Cogunan", "Dayap", "Kaya-an", "Kaylaway", "Kayrilaw", "Latag", "Looc",
        "Lumbangan", "Malawangan", "Mataas Na Pulo", "Mawagitan", "Natipuan",
        "Pantalan", "Papaya", "Putat", "Reparo", "Talangan", "Tumalim", "Utod", "Wawa"
    )

    // Key Barangay center coordinates inside Nasugbu for map navigation
    val NASUGBU_BARANGAY_LOCATIONS = mapOf(
        "Nasugbu Center (Poblacion)" to LatLng(14.07326, 120.63265),
        "Wawa" to LatLng(14.07850, 120.62620),
        "Natipuan" to LatLng(14.12000, 120.59000),
        "Bucana" to LatLng(14.08500, 120.64000),
        "Kaylaway" to LatLng(14.06200, 120.78000),
        "Bilaran" to LatLng(14.07100, 120.66500),
        "Aga" to LatLng(14.06800, 120.72000),
        "Looc" to LatLng(14.15000, 120.61500),
        "Calayo" to LatLng(14.13500, 120.57500),
        "Balaytigue" to LatLng(14.16000, 120.59500),
        "Lumbangan" to LatLng(14.04500, 120.65000)
    )

    // Administrative boundary coordinates of Nasugbu Municipality, Batangas
    val DEFAULT_POINTS = listOf(
        LatLng(14.06330, 120.34778),
        LatLng(14.04771, 120.34844),
        LatLng(14.03741, 120.34990),
        LatLng(14.02223, 120.35363),
        LatLng(14.01239, 120.35710),
        LatLng(13.99828, 120.36371),
        LatLng(13.97773, 120.39345),
        LatLng(13.94731, 120.43470),
        LatLng(13.91807, 120.47494),
        LatLng(14.01571, 120.55291),
        LatLng(14.06008, 120.56211),
        LatLng(14.05785, 120.62467),
        LatLng(14.05468, 120.63358),
        LatLng(14.05438, 120.64440),
        LatLng(14.04458, 120.64713),
        LatLng(14.04804, 120.66093),
        LatLng(14.05009, 120.67577),
        LatLng(14.04143, 120.68294),
        LatLng(14.05995, 120.69700),
        LatLng(14.05936, 120.71134),
        LatLng(14.06042, 120.72356),
        LatLng(14.06268, 120.73645),
        LatLng(14.06962, 120.74954),
        LatLng(14.07519, 120.76286),
        LatLng(14.07969, 120.78065),
        LatLng(14.07618, 120.79438),
        LatLng(14.06362, 120.79333),
        LatLng(14.04979, 120.79643),
        LatLng(14.04035, 120.80181),
        LatLng(14.05184, 120.81220),
        LatLng(14.06208, 120.82297),
        LatLng(14.05808, 120.84446),
        LatLng(14.07512, 120.84506),
        LatLng(14.08903, 120.83890),
        LatLng(14.09673, 120.82504),
        LatLng(14.10359, 120.81073),
        LatLng(14.10222, 120.79001),
        LatLng(14.11561, 120.78218),
        LatLng(14.12997, 120.77656),
        LatLng(14.13510, 120.76040),
        LatLng(14.13398, 120.74021),
        LatLng(14.14000, 120.72453),
        LatLng(14.14602, 120.70802),
        LatLng(14.16392, 120.70593),
        LatLng(14.17062, 120.69756),
        LatLng(14.17171, 120.67769),
        LatLng(14.18584, 120.68570),
        LatLng(14.20004, 120.68438),
        LatLng(14.20949, 120.67329),
        LatLng(14.21627, 120.64452),
        LatLng(14.22351, 120.63079),
        LatLng(14.21555, 120.63458),
        LatLng(14.21173, 120.61246),
        LatLng(14.22272, 120.58820),
        LatLng(14.2224, 120.56652),
        LatLng(14.22832, 120.54304),
        LatLng(14.24054, 120.47350),
        LatLng(14.24889, 120.44880),
        LatLng(14.23509, 120.44402),
        LatLng(14.22499, 120.44159),
        LatLng(14.21475, 120.43995),
        LatLng(14.20235, 120.43726),
        LatLng(14.18725, 120.43458),
        LatLng(14.17915, 120.41919),
        LatLng(14.17053, 120.40537),
        LatLng(14.15765, 120.38974),
        LatLng(14.14711, 120.37972),
        LatLng(14.13417, 120.36994),
        LatLng(14.11038, 120.35738),
        LatLng(14.09512, 120.35226),
        LatLng(14.08466, 120.34990),
        LatLng(14.07406, 120.34840),
        LatLng(14.06330, 120.34778)
    )

    var points: List<LatLng> = DEFAULT_POINTS
        private set

    fun resetToDefault() {
        points = DEFAULT_POINTS
    }

    fun setCustomPoints(custom: List<LatLng>) {
        if (custom.size >= 3) {
            points = custom
        }
    }

    val NASUGBU_BOUNDS: LatLngBounds
        get() {
            val builder = LatLngBounds.builder()
            points.forEach { builder.include(it) }
            return builder.build()
        }

    fun contains(point: LatLng): Boolean {
        var inside = false
        var previousIndex = points.lastIndex

        for (index in points.indices) {
            val current = points[index]
            val previous = points[previousIndex]
            val onSegment = isOnSegment(point, previous, current)
            if (onSegment) return true

            val crossesLatitude = (current.latitude > point.latitude) !=
                (previous.latitude > point.latitude)
            if (crossesLatitude) {
                val crossingLongitude = (previous.longitude - current.longitude) *
                    (point.latitude - current.latitude) /
                    (previous.latitude - current.latitude) + current.longitude
                if (point.longitude < crossingLongitude) inside = !inside
            }
            previousIndex = index
        }
        return inside
    }

    fun contains(lat: Double, lng: Double): Boolean = contains(LatLng(lat, lng))

    private fun isOnSegment(point: LatLng, start: LatLng, end: LatLng): Boolean {
        val cross = (point.latitude - start.latitude) * (end.longitude - start.longitude) -
            (point.longitude - start.longitude) * (end.latitude - start.latitude)
        if (kotlin.math.abs(cross) > 1e-8) return false
        return point.longitude >= minOf(start.longitude, end.longitude) - 1e-8 &&
            point.longitude <= maxOf(start.longitude, end.longitude) + 1e-8 &&
            point.latitude >= minOf(start.latitude, end.latitude) - 1e-8 &&
            point.latitude <= maxOf(start.latitude, end.latitude) + 1e-8
    }

    fun parseBBBikeExtract(rawInput: String): List<LatLng>? {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return null

        try {
            // Option 1: GeoJSON object or array
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                val extracted = mutableListOf<LatLng>()
                if (trimmed.startsWith("{")) {
                    val root = JSONObject(trimmed)
                    val features = root.optJSONArray("features")
                    if (features != null && features.length() > 0) {
                        for (i in 0 until features.length()) {
                            val feature = features.getJSONObject(i)
                            val geometry = feature.optJSONObject("geometry")
                            val coordinates = geometry?.optJSONArray("coordinates")
                            if (coordinates != null) {
                                parseCoordinatesArray(coordinates, extracted)
                            }
                        }
                    } else {
                        val geometry = root.optJSONObject("geometry")
                        val coordinates = geometry?.optJSONArray("coordinates") ?: root.optJSONArray("coordinates")
                        if (coordinates != null) {
                            parseCoordinatesArray(coordinates, extracted)
                        }
                    }
                } else {
                    val jsonArray = JSONArray(trimmed)
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.optJSONObject(i)
                        if (item != null && item.has("lat") && item.has("lng")) {
                            extracted.add(LatLng(item.getDouble("lat"), item.getDouble("lng")))
                        }
                    }
                }
                if (extracted.size >= 3) return extracted
            }

            // Option 2: BBBike Bounding Box string (sw_lat,sw_lng,ne_lat,ne_lng)
            val parts = trimmed.split(",").map { it.trim() }
            if (parts.size == 4) {
                val minLat = parts[0].toDoubleOrNull()
                val minLng = parts[1].toDoubleOrNull()
                val maxLat = parts[2].toDoubleOrNull()
                val maxLng = parts[3].toDoubleOrNull()
                if (minLat != null && minLng != null && maxLat != null && maxLng != null) {
                    return listOf(
                        LatLng(minLat, minLng),
                        LatLng(minLat, maxLng),
                        LatLng(maxLat, maxLng),
                        LatLng(maxLat, minLng),
                        LatLng(minLat, minLng)
                    )
                }
            }

            // Option 3: BBBike .poly format (lines of "  longitude  latitude")
            val polyPoints = mutableListOf<LatLng>()
            trimmed.lines().forEach { line ->
                val tokens = line.trim().split("\\s+".toRegex())
                if (tokens.size >= 2) {
                    val lng = tokens[0].toDoubleOrNull()
                    val lat = tokens[1].toDoubleOrNull()
                    if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                        polyPoints.add(LatLng(lat, lng))
                    }
                }
            }
            if (polyPoints.size >= 3) return polyPoints

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun parseCoordinatesArray(jsonArray: JSONArray, outList: MutableList<LatLng>) {
        if (jsonArray.length() == 0) return
        val first = jsonArray.get(0)
        if (first is JSONArray) {
            for (i in 0 until jsonArray.length()) {
                parseCoordinatesArray(jsonArray.getJSONArray(i), outList)
            }
        } else if (jsonArray.length() >= 2) {
            val lng = jsonArray.getDouble(0)
            val lat = jsonArray.getDouble(1)
            outList.add(LatLng(lat, lng))
        }
    }

    fun loadFromAssets(context: android.content.Context, filename: String = "nasugbu.geojson"): Boolean {
        try {
            val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
            val parsedPoints = parseBBBikeExtract(jsonString)
            if (parsedPoints != null && parsedPoints.size >= 3) {
                setCustomPoints(parsedPoints)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
