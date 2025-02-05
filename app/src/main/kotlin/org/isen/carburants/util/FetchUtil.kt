package org.isen.carburants.util

import ApiResponse
import FuelStation
import GeoPoint
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


fun fetchFuelStations(
    city: String? = null,
    fuelType: String? = null,
    hasToilet: Boolean? = null,
    hasAirPump: Boolean? = null,
    hasFoodStore: Boolean? = null
): List<FuelStation> {
    // Base API URL
    val baseUrl = "https://public.opendatasoft.com/api/explore/v2.1/catalog/datasets/prix-des-carburants-j-1/records"

    // Construct the `where` query dynamically
    val filters = mutableListOf<String>()

    city?.let { filters.add("com_arm_name like \"${it.uppercase()}\"") }
    fuelType?.let { filters.add("fuel like \"${it}\"") }
    hasToilet?.let { if (it) filters.add("services like \"toilet\"") }
    hasAirPump?.let { if (it) filters.add("services like \"Station de gonflage\"") }
    hasFoodStore?.let { if (it) filters.add("services like \"boutique alimentaire\"") }

    // Combine filters into a URL parameter
    val whereClause =
        if (filters.isNotEmpty()) "where=" + filters.joinToString(" AND ") { URLEncoder.encode(it, "UTF-8") } else ""

    // Construct final URL
    val finalUrl = "$baseUrl?$whereClause&limit=5"

    println("Fetching data from: $finalUrl") // Debugging

    val connection = URL(finalUrl).openConnection() as HttpURLConnection
    connection.requestMethod = "GET"

    val jsonResponse = connection.inputStream.bufferedReader().use { it.readText() }
    val apiResponse = Json { ignoreUnknownKeys = true }.decodeFromString<ApiResponse>(jsonResponse)

    return apiResponse.results.map { mapToFuelStation(it) }
}


@Serializable
data class NominatimResponse(
    val lat: String,
    val lon: String
)

// For better performance, it should be done in batch
fun fetchCoordinatesFromAddress(address: String, city: String): GeoPoint? {
    val fullAddress = "$address, $city"

    val encodedAddress = URLEncoder.encode(fullAddress, "UTF-8")
    val url = "https://nominatim.openstreetmap.org/search?q=$encodedAddress&format=json&limit=1"

    return try {
        logger.info("Fetching coordinates from: $url")

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        connection.inputStream.use { inputStream ->
            val reader = InputStreamReader(inputStream)
            val response = reader.readText()

            val result =
                Json { ignoreUnknownKeys = true }.decodeFromString<List<NominatimResponse>>(response).firstOrNull()
            result?.let {
                GeoPoint(it.lat.toDouble(), it.lon.toDouble())
            }
        }
    } catch (e: Exception) {
        println("Error fetching coordinates: ${e.message}")
        null
    }
}