import kotlinx.serialization.Serializable

@Serializable
data class FuelStation(
    val name: String?,
    val address: String,
    val city: String,
    val fuelPrices: Map<String, Double>,
    val geo_point: GeoPoint?,
    val hasToilet: Boolean,
    val hasAirPump: Boolean,
    val hasFoodStore: Boolean
)

@Serializable
data class FuelStationResponse(
    val name: String?,
    val address: String,
    val com_arm_name: String,
    val fuel: List<String>,
    val price_gazole: Double?,
    val price_sp95: Double?,
    val price_sp98: Double?,
    val price_gplc: Double?,
    val price_e10: Double?,
    val price_e85: Double?,
    val services: List<String>?,
    val geo_point: GeoPoint?
)

@Serializable
data class GeoPoint(
    val lat: Double,
    val lon: Double
)

@Serializable
data class ApiResponse(
    val total_count: Int,
    val results: List<FuelStationResponse>
)




