package com.enioka.gowhere

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enioka.gowhere.model.ActivitiesAdapter
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.navigation.fragment.findNavController
import com.enioka.gowhere.model.Coordinates
import com.enioka.gowhere.model.DistanceCalculator
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

class InformationsFragment : Fragment() {

    private lateinit var activitiesRecyclerView: RecyclerView
    private lateinit var activitiesAdapter: ActivitiesAdapter
    private lateinit var currentPositionTextView: TextView
    private lateinit var cityPositionTextView: TextView
    private lateinit var mapView: MapView
    private lateinit var temperatureTextView: TextView
    private lateinit var distanceTextView: TextView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info_destination, container, false)

        // Initialisation de la configuration d'OSMDroid
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )

        // Initialisation des TextViews
        currentPositionTextView = view.findViewById(R.id.currentPositionTextView)
        cityPositionTextView = view.findViewById(R.id.cityPositionTextView)
        distanceTextView = view.findViewById(R.id.distanceTextView)

        // Initialisation du RecyclerView
        activitiesRecyclerView = view.findViewById(R.id.activitiesRecyclerView)
        activitiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        activitiesAdapter = ActivitiesAdapter()
        activitiesRecyclerView.adapter = activitiesAdapter

        // Initialisation de la carte
        mapView = view.findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true) // Activer les gestes multitouch
        mapView.controller.setZoom(15.0)

        // Récupérer la ville, le pays et ses activités
        val sharedPrefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val randomCity = sharedPrefs.getString("random_city", "Inconnue")
        val country = sharedPrefs?.getString("country_$randomCity", "Pays inconnu") ?: "Pays inconnu"
        val activities = sharedPrefs.getStringSet("activities_$randomCity", setOf())

        // Mettre à jour le TextView de la ville
        val cityTextView: TextView = view.findViewById(R.id.destinationEditText)
        cityTextView.text = randomCity

        // Mettre à jour le TextView du pays
        val countryTextView: TextView = view.findViewById(R.id.country_result)
        countryTextView.text = country

        // Mettre à jour le RecyclerView
        activitiesAdapter.submitList(activities?.toList() ?: emptyList())

        // Afficher les coordonnées
        displayCurrentLocation(sharedPrefs, randomCity)
        displayCityCoordinates(sharedPrefs, randomCity)

        // Mettre à jour le TextView de la distance
        displayDistance(sharedPrefs, randomCity)

        // Bouton pour revenir à l'accueil
        view.findViewById<Button>(R.id.btnBackToHome).setOnClickListener {
            findNavController().navigate(R.id.action_infodestinationFragment_to_mainFragment)
        }

        return view
    }

    private fun displayCurrentLocation(sharedPrefs: SharedPreferences, cityName: String?) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (location != null) {
                val formattedLatitude = String.format("%.4f", location.latitude)
                val formattedLongitude = String.format("%.4f", location.longitude)
                currentPositionTextView.text =
                    "Position actuelle : $formattedLatitude, $formattedLongitude"
                val currentGeoPoint = GeoPoint(location.latitude, location.longitude)
                updateMapWithPolyline(currentGeoPoint, sharedPrefs, cityName)
            } else {
                currentPositionTextView.text = "Recherche de la position..."
                requestLocationUpdates(sharedPrefs, cityName)
            }

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun displayCityCoordinates(sharedPrefs: SharedPreferences, cityName: String?) {
        val coordinates = sharedPrefs.getString("coordinates_$cityName", null)
        if (coordinates != null) {
            cityPositionTextView.text = "Coordonnées : $coordinates"
            val latLng = coordinates.split(",")
            val cityGeoPoint = GeoPoint(latLng[0].toDouble(), latLng[1].toDouble())
        } else {
            cityPositionTextView.text = "Coordonnées : Inconnue"
        }
    }

    private fun displayDistance(sharedPrefs: SharedPreferences, cityName: String?) {
        var coordinates_current: Coordinates? = null // Initialisation de la variable
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (location != null) {
                val formattedLatitude = String.format("%.4f", location.latitude)
                val formattedLongitude = String.format("%.4f", location.longitude)
                coordinates_current = Coordinates(formattedLatitude.toDouble(), formattedLongitude.toDouble())
            } else {
                currentPositionTextView.text = "Recherche de la position..."
                requestLocationUpdates(sharedPrefs, cityName)
            }

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Vérification si les coordonnées de la ville sont présentes
        val coordinates = sharedPrefs.getString("coordinates_$cityName", null)
        if (coordinates != null) {
            val latLng = coordinates.split(",")
            val coordinates_city = Coordinates(latLng[0].toDouble(), latLng[1].toDouble())

            // Vérification si les coordonnées actuelles sont disponibles
            if (coordinates_current != null) {
                // Appel correct de la méthode pour calculer la distance
                val distance = DistanceCalculator.calculateDistance(coordinates_current, coordinates_city)
                distanceTextView.text = "Distance : ${String.format("%.2f", distance)} km"
            } else {
                currentPositionTextView.text = "Position actuelle non disponible"
            }
        } else {
            cityPositionTextView.text = "Coordonnées de la ville : Inconnues"
        }
    }

    private fun updateMapWithPolyline(currentGeoPoint: GeoPoint?, sharedPrefs: SharedPreferences, cityName: String?) {
        var cityGeoPoint: GeoPoint? = null  // Déclare cityGeoPoint comme une variable mutable

        // Récupération des coordonnées de la ville depuis les SharedPreferences
        val coordinates = sharedPrefs.getString("coordinates_$cityName", null)
        if (coordinates != null) {
            cityPositionTextView.text = "Coordonnées : $coordinates"
            val latLng = coordinates.split(",")
            cityGeoPoint = GeoPoint(latLng[0].toDouble(), latLng[1].toDouble())
        } else {
            cityPositionTextView.text = "Coordonnées : Inconnue"
        }

        // Si aucune des deux positions n'est disponible, on quitte la fonction
        if (currentGeoPoint == null && cityGeoPoint == null) return

        // Nettoyer les overlays de la carte avant d'ajouter les nouveaux marqueurs et polyligne
        mapView.overlays.clear()

        // Ajouter un marqueur pour la position actuelle
        currentGeoPoint?.let {
            val currentMarker = Marker(mapView)
            currentMarker.position = it
            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            currentMarker.title = "Position actuelle"
            mapView.overlays.add(currentMarker)
        }

        // Ajouter un marqueur pour la destination
        cityGeoPoint?.let {
            val cityMarker = Marker(mapView)
            cityMarker.position = it
            cityMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            cityMarker.title = "Destination"
            mapView.overlays.add(cityMarker)
        }

        // Si les deux points sont disponibles, on trace une polyligne entre eux
        if (currentGeoPoint != null && cityGeoPoint != null) {
            val polyline = Polyline()
            polyline.addPoint(currentGeoPoint)
            polyline.addPoint(cityGeoPoint)
            polyline.color = android.graphics.Color.BLUE
            polyline.width = 5f
            mapView.overlays.add(polyline)
        }

        // Centrer la carte sur la position
        val centerPoint = cityGeoPoint ?: currentGeoPoint
        mapView.controller.setCenter(centerPoint)
    }

    private fun requestLocationUpdates(sharedPrefs: SharedPreferences, cityName: String?) {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                object : android.location.LocationListener {
                    override fun onLocationChanged(location: android.location.Location) {
                        val currentGeoPoint = GeoPoint(location.latitude, location.longitude)
                        currentPositionTextView.text =
                            "Position actuelle : ${currentGeoPoint.latitude}, ${currentGeoPoint.longitude}"
                        updateMapWithPolyline(currentGeoPoint, sharedPrefs, cityName)
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {
                        currentPositionTextView.text = "GPS désactivé."
                    }
                }
            )
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
}