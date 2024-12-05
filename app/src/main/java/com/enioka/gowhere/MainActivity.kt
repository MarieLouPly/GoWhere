package com.enioka.gowhere

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Demande de permission pour la localisation
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        // Vérifier si la permission est déjà accordée
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission déjà accordée, rien à faire
            Toast.makeText(this, "Permission de localisation déjà accordée", Toast.LENGTH_SHORT).show()
        } else {
            // Demander la permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Gérer la réponse à la demande de permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée
                Toast.makeText(this, "Permission de localisation accordée", Toast.LENGTH_SHORT).show()
            } else {
                // Permission refusée
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
