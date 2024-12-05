package com.enioka.gowhere.model
import kotlin.math.*

data class Coordinates(val latitude: Double, val longitude: Double)

class DistanceCalculator {

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0 // Rayon de la Terre en kilomètres

        // Fonction pour calculer la distance entre deux coordonnées
        fun calculateDistance(coord1: Coordinates, coord2: Coordinates): Double {
            val lat1Rad = Math.toRadians(coord1.latitude)
            val lon1Rad = Math.toRadians(coord1.longitude)
            val lat2Rad = Math.toRadians(coord2.latitude)
            val lon2Rad = Math.toRadians(coord2.longitude)

            // Formule de Haversine pour la distance
            val deltaLat = lat2Rad - lat1Rad
            val deltaLon = lon2Rad - lon1Rad
            val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            return EARTH_RADIUS_KM * c
        }
    }
}