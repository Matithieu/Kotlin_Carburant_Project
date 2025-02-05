package org.isen.carburants

import FuelStation
import GeoPoint
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.TextInputDialog
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.isen.carburants.util.fetchCoordinatesFromAddress
import org.isen.carburants.util.fetchFuelStations
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class FuelStationApp : Application() {
    val logger = LogManager.getLogger("FuelStationApp")

    override fun start(primaryStage: Stage) {
        // Create the city input dialog
        val cityDialog = TextInputDialog("PARIS") // Default city name "Paris"
        cityDialog.title = "City Name"
        cityDialog.headerText = "Enter the name of the city"
        cityDialog.contentText = "City:"

        // Show the dialog and get the result
        val result = cityDialog.showAndWait().orElse("PARIS") // Default to "PARIS" if user cancels
        logger.info("City entered: $result")

        // Fetch the coordinates for the city
        val geoPoint = fetchCoordinatesFromAddress(result, result) // Use city name for both address and city

        val imageView = ImageView().apply {
            fitWidth = 800.0
            fitHeight = 300.0
            isPreserveRatio = true
        }

        // Fetch the fuel stations based on the entered city
        val fuelStations = fetchFuelStations(city = result)
        logger.info("Found ${fuelStations.size} fuel stations in $result")
        fuelStations.forEach { logger.info(it) }

        // Load map with the fuel stations and dynamic center
        val mapImage = loadMapImage(fuelStations, geoPoint)
        imageView.image = mapImage

        // Setup the scene
        val vbox = VBox(imageView)
        val scene = Scene(vbox, 820.0, 320.0)

        primaryStage.title = "Geoapify Static Map"
        primaryStage.scene = scene
        primaryStage.show()
    }


    private  fun loadMapImage(fuelStations: List<FuelStation>, centerGeoPoint: GeoPoint?): Image {
        val baseUrl = "https://maps.geoapify.com/v1/staticmap"
        val style = "osm-bright"
        val width = 800
        val height = 300
        val zoom = 10
        val apiKey = "4656c6a248fa4763b392b80d52b47206" // https://myprojects.geoapify.com/api

        // Use dynamic center coordinates (from fuel stations or city)
        val centerLon = centerGeoPoint?.lon ?: 2.319834531483585  // Default longitude if no valid point (Paris)
        val centerLat = centerGeoPoint?.lat ?: 48.830320964795796  // Default latitude if no valid point (Paris)

        // Create markers and ensure they are properly URL-encoded
        val markers = fuelStations
            .filter { it.geo_point?.lat != null }
            .take(10)  // Limit to 10 markers to avoid oversized URL. Should be limited in the fetchFuelStations function
            .joinToString("&") { station ->
                val geo = station.geo_point!!
                "marker=lonlat:${geo.lon},${geo.lat}"
            }

        logger.info("Markers: $markers")

        val fullUrl =
            "$baseUrl?style=$style&width=$width&height=$height&center=lonlat:$centerLon,$centerLat&zoom=$zoom&$markers&apiKey=$apiKey"

        logger.info("Loading map from: $fullUrl")

        return try {
            val url = URL(fullUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000  // Optional: Set timeout
            connection.readTimeout = 10000    // Optional: Set read timeout

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream: InputStream = connection.inputStream
                Image(ByteArrayInputStream(inputStream.readBytes()))
            } else {
                logger.error("Error loading map: Server returned HTTP response code: ${connection.responseCode}")
                Image("default_error_image_path")  // Use a fallback image if there’s an error
            }
        } catch (e: Exception) {
            logger.error("Exception while loading map: ${e.message}")
            Image("default_error_image_path")  // Use a fallback image if there’s an exception
        }
    }


}

fun main() {
    Application.launch(FuelStationApp::class.java)
}
