package com.example.ben.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.ben.R
import com.example.ben.databinding.ActivityMapBinding
import com.example.ben.models.Hive
import com.example.ben.utils.FirebaseUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var action: String = ""
    private var currentCircle: Circle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        action = intent.getStringExtra("ACTION") ?: ""
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            // Fallback to default
            val defaultLocation = LatLng(12.9716, 77.5946)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f))
            updateCircle(defaultLocation)
        }

        loadHives()

        // Always allow pinning a hive on click or long press
        mMap.setOnMapClickListener { latLng ->
            if (action == "ADD_HIVE") {
                showAddHiveDialog(latLng)
            }
        }

        mMap.setOnMapLongClickListener { latLng ->
            if (action == "ADD_HIVE") {
                showAddHiveDialog(latLng)
            }
        }

        if (action == "ADD_HIVE") {
            com.google.android.material.snackbar.Snackbar.make(binding.root, "Tap on map to pin your hive location", com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE)
                .setAction("Dismiss") { }
                .show()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                updateCircle(currentLatLng)
            } else {
                val defaultLocation = LatLng(12.9716, 77.5946)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f))
                updateCircle(defaultLocation)
            }
        }
    }

    private fun updateCircle(center: LatLng) {
        currentCircle?.remove()
        currentCircle = mMap.addCircle(CircleOptions()
            .center(center)
            .radius(2000.0)
            .strokeWidth(2f)
            .strokeColor(Color.BLUE)
            .fillColor(0x220000FF))
    }

    private fun showAddHiveDialog(latLng: LatLng) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Hive")
            .setMessage("Do you want to add a hive at this location?")
            .setPositiveButton("Yes") { _, _ ->
                addHiveToDatabase(latLng)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun addHiveToDatabase(latLng: LatLng) {
        val hiveId = FirebaseUtils.hivesRef().push().key ?: return
        val hive = Hive(
            id = hiveId,
            beekeeperId = FirebaseUtils.currentUserUid ?: "",
            name = "My Bee Hive",
            latitude = latLng.latitude,
            longitude = latLng.longitude,
            description = "Protected area"
        )

        FirebaseUtils.hivesRef().child(hiveId).setValue(hive)
            .addOnSuccessListener {
                Toast.makeText(this, "Hive pinned successfully!", Toast.LENGTH_SHORT).show()
                // The onDataChange listener will pick this up and draw the marker
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to pin hive: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadHives() {
        FirebaseUtils.hivesRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!::mMap.isInitialized) return
                
                // Instead of full clear, we could manage markers, but clear is safer for basic sync
                mMap.clear()
                
                // Redraw the circle if we have a center, else use Bangalore
                val circleCenter = currentCircle?.center ?: LatLng(12.9716, 77.5946)
                updateCircle(circleCenter)

                var count = 0
                for (hiveSnapshot in snapshot.children) {
                    val hive = hiveSnapshot.getValue(Hive::class.java)
                    if (hive != null) {
                        val pos = LatLng(hive.latitude, hive.longitude)
                        mMap.addMarker(MarkerOptions()
                            .position(pos)
                            .title(hive.name)
                            .snippet(hive.description)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                        count++
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapActivity, "Failed to load hives: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
