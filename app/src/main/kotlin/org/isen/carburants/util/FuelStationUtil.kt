package org.isen.carburants.util

import FuelStation
import FuelStationResponse
import GeoPoint
import org.apache.logging.log4j.LogManager
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

val logger = LogManager.getLogger("FuelStationApp")


fun mapToFuelStation(response: FuelStationResponse): FuelStation {
    val geo = response.geo_point ?: fetchCoordinatesFromAddress(response.address, response.com_arm_name)

    val fuelPrices = mutableMapOf<String, Double>()
    response.price_gazole?.let { fuelPrices["Gazole"] = it }
    response.price_sp95?.let { fuelPrices["SP95"] = it }
    response.price_sp98?.let { fuelPrices["SP98"] = it }
    response.price_gplc?.let { fuelPrices["GPLc"] = it }
    response.price_e10?.let { fuelPrices["E10"] = it }
    response.price_e85?.let { fuelPrices["E85"] = it }

    return FuelStation(
        name = response.name,
        address = response.address,
        city = response.com_arm_name,
        fuelPrices = fuelPrices,
        geo_point = geo,
        hasToilet = response.services?.contains("toilet") == true,
        hasAirPump = response.services?.contains("Station de gonflage") == true,
        hasFoodStore = response.services?.contains("boutique alimentaire") == true
    )
}

fun parseXml(file: File): List<FuelStation> {
    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
    doc.documentElement.normalize()

    val pdvList = mutableListOf<FuelStation>()

    val pdvNodes = doc.getElementsByTagName("pdv")
    for (i in 0 until pdvNodes.length) {
        val pdv = pdvNodes.item(i) as Element
        val address = pdv.getElementsByTagName("adresse").item(0).textContent
        val city = pdv.getElementsByTagName("ville").item(0).textContent

        val fuelPrices = mutableMapOf<String, Double>()
        val prices = pdv.getElementsByTagName("prix")
        for (j in 0 until prices.length) {
            val priceElement = prices.item(j) as Element
            val fuelType = priceElement.getAttribute("nom")
            val value = priceElement.getAttribute("valeur").toDouble()
            fuelPrices[fuelType] = value
        }

        val services = pdv.getElementsByTagName("service")
        val serviceList = mutableListOf<String>()
        for (j in 0 until services.length) {
            serviceList.add(services.item(j).textContent)
        }

        pdvList.add(
            FuelStation(
                name = null, // Adjust this if needed
                address = address,
                city = city,
                fuelPrices = fuelPrices,
                geo_point = GeoPoint(
                    lat = pdv.getAttribute("latitude").toDouble(),
                    lon = pdv.getAttribute("longitude").toDouble()
                ),
                hasToilet = serviceList.contains("toilet"),
                hasAirPump = serviceList.contains("Station de gonflage"),
                hasFoodStore = serviceList.contains("boutique alimentaire")
            )
        )
    }

    return pdvList
}

fun filterFuelStations(
    stations: List<FuelStation>,
    city: String? = null,
    fuelType: String? = null,
    hasToilet: Boolean? = null,
    hasAirPump: Boolean? = null,
    hasFoodStore: Boolean? = null,
    limit: Int = 5 // Limit for the XML data
): List<FuelStation> {
    return stations.filter { station ->
        val matchesCity = city?.let { station.city.equals(it, ignoreCase = true) } ?: true
        val matchesFuelType = fuelType?.let { station.fuelPrices.containsKey(it) } ?: true
        val matchesToilet = hasToilet?.let { station.hasToilet == it } ?: true
        val matchesAirPump = hasAirPump?.let { station.hasAirPump == it } ?: true
        val matchesFoodStore = hasFoodStore?.let { station.hasFoodStore == it } ?: true

        matchesCity && matchesFuelType && matchesToilet && matchesAirPump && matchesFoodStore
    }.take(limit)
}
