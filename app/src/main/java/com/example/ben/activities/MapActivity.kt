package com.example.ben.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ben.R
import com.example.ben.databinding.ActivityMapBinding
import com.example.ben.models.Hive
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.AuthViewModel
import com.example.ben.viewmodels.MainViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "MapActivityDebug"
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private val mainViewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    private var action: String = ""
    private var userRole: String = ""
    private var currentLatLng: LatLng? = null
    private var currentCircle: Circle? = null
    private var targetLat: Double = 0.0
    private var targetLng: Double = 0.0
    
    // Track markers to avoid flickering and improve performance
    private val hiveMarkers = mutableMapOf<String, Marker>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            enableMyLocation()
        } else {
            Toast.makeText(this, "Location permission required for best experience", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        action = intent.getStringExtra("ACTION") ?: ""
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        targetLat = intent.getDoubleExtra("LAT", 0.0)
        targetLng = intent.getDoubleExtra("LNG", 0.0)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupObservers()
        
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        FirebaseUtils.currentUserUid?.let { authViewModel.fetchUserData(it) }
    }

    private fun setupObservers() {
        authViewModel.userData.observe(this) { user ->
            user?.let { userRole = it.role }
        }

        mainViewModel.hives.observe(this) { hives ->
            if (::mMap.isInitialized) {
                updateMapMarkers(hives)
            }
        }

        mainViewModel.loading.observe(this) { isLoading ->
            // Subtle loading, don't block interaction
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        mainViewModel.status.observe(this) { status ->
            status?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                mainViewModel.clearStatus()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = false
        }

        if (targetLat != 0.0 && targetLng != 0.0) {
            val target = LatLng(targetLat, targetLng)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 16f))
        } else {
            checkLocationPermissions()
        }
        mainViewModel.fetchAllHives()

        mMap.setOnMapClickListener { latLng ->
            if (userRole == "Beekeeper" || action == "ADD_HIVE") {
                showPinDialog(latLng)
            }
        }
        
        if (action == "ADD_HIVE") {
            Snackbar.make(binding.root, "Tap on map to pin your hive location", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        mMap.isMyLocationEnabled = true
        getCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                currentLatLng = latLng
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                drawRadiusCircle(latLng)
            } else {
                requestFreshLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000)
            .setMaxUpdates(1)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    currentLatLng = latLng
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                    drawRadiusCircle(latLng)
                }
            }
        }, null)
    }

    private fun drawRadiusCircle(center: LatLng) {
        currentCircle?.remove()
        currentCircle = mMap.addCircle(CircleOptions()
            .center(center)
            .radius(2000.0)
            .strokeWidth(2f)
            .strokeColor(Color.parseColor("#4CAF50"))
            .fillColor(Color.parseColor("#224CAF50")))
    }

    private fun updateMapMarkers(hives: List<Hive>) {
        if (!::mMap.isInitialized) return
        Log.d(TAG, "updateMapMarkers: Received ${hives.size} hives from Firebase")
        
        // Find hives that are no longer in the list
        val currentHiveIds = hives.map { it.id }.toSet()
        val markersToRemove = hiveMarkers.filter { it.key !in currentHiveIds }
        Log.d(TAG, "updateMapMarkers: Removing ${markersToRemove.size} stale markers")
        markersToRemove.forEach { (id, marker) ->
            marker.remove()
            hiveMarkers.remove(id)
        }

        // Add or update markers
        hives.forEach { hive ->
            val pos = LatLng(hive.latitude, hive.longitude)
            if (hiveMarkers.containsKey(hive.id)) {
                Log.d(TAG, "updateMapMarkers: Updating existing marker for hive: ${hive.name}")
                hiveMarkers[hive.id]?.position = pos
            } else {
                Log.d(TAG, "updateMapMarkers: Adding NEW marker for hive: ${hive.name} at $pos")
                val marker = mMap.addMarker(MarkerOptions()
                    .position(pos)
                    .title(hive.name)
                    .snippet("Status: ${hive.status}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                if (marker != null) {
                    hiveMarkers[hive.id] = marker
                }
            }
        }
    }

    private fun showPinDialog(latLng: LatLng) {
        Log.d(TAG, "showPinDialog: Marker selected at $latLng")
        // Instant visual feedback: place a temporary marker
        val tempMarker = mMap.addMarker(MarkerOptions()
            .position(latLng)
            .title("Selected Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Pin Hive Here?")
            .setMessage("Lat: ${String.format("%.5f", latLng.latitude)}\nLng: ${String.format("%.5f", latLng.longitude)}")
            .setPositiveButton("Proceed") { _, _ ->
                val intent = Intent(this, AddHiveActivity::class.java)
                intent.putExtra("LAT", latLng.latitude)
                intent.putExtra("LNG", latLng.longitude)
                startActivity(intent)
                tempMarker?.remove()
            }
            .setNegativeButton("Cancel") { _, _ ->
                tempMarker?.remove()
            }
            .setOnCancelListener { tempMarker?.remove() }
            .show()
    }
}
