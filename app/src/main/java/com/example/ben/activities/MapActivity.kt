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
    
    // Map to track active markers and prevent flickering
    private val hiveMarkers = mutableMapOf<String, Marker>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            enableMyLocation()
        } else {
            Toast.makeText(this, "Location permission denied. Map might not center correctly.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: Map Screen Initializing")
        action = intent.getStringExtra("ACTION") ?: ""
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupObservers()
        
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        FirebaseUtils.currentUserUid?.let { authViewModel.fetchUserData(it) }
    }

    private fun setupObservers() {
        authViewModel.userData.observe(this) { user ->
            user?.let { 
                userRole = it.role
                Log.d(TAG, "setupObservers: User role confirmed: $userRole")
            }
        }

        mainViewModel.hives.observe(this) { hives ->
            if (::mMap.isInitialized) {
                Log.d(TAG, "setupObservers: Received ${hives.size} hives from LiveData. Updating markers...")
                updateMapMarkers(hives)
            }
        }

        mainViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) Log.d(TAG, "setupObservers: Loading shown")
            else Log.d(TAG, "setupObservers: Loading hidden")
        }

        mainViewModel.status.observe(this) { status ->
            status?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                mainViewModel.clearStatus()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady: Map Loaded Successfully")
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = true
        }

        checkLocationPermissions()
        
        // Load existing markers
        mainViewModel.fetchAllHives()

        // Handle Map Taps for Pinning
        mMap.setOnMapClickListener { latLng ->
            Log.d(TAG, "onMapClick: User tapped at $latLng")
            if (userRole == "Beekeeper" || action == "ADD_HIVE") {
                showPinDialog(latLng)
            } else {
                Toast.makeText(this, "Only beekeepers can pin hive locations", Toast.LENGTH_SHORT).show()
            }
        }
        
        if (action == "ADD_HIVE") {
            Snackbar.make(binding.root, "Tap on map to pin your hive location", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK") { }
                .show()
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        mMap.isMyLocationEnabled = true
        getCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation: Fetching GPS location...")
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                currentLatLng = latLng
                Log.d(TAG, "getCurrentLocation: SUCCESS - $latLng")
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                drawRadiusCircle(latLng)
            } else {
                Log.w(TAG, "getCurrentLocation: FAILED - Location is null")
                // Fallback to center of India if GPS is off
                val defaultLatLng = LatLng(20.5937, 78.9629)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 5f))
            }
        }
    }

    private fun drawRadiusCircle(center: LatLng) {
        currentCircle?.remove()
        currentCircle = mMap.addCircle(CircleOptions()
            .center(center)
            .radius(2000.0) // 2km
            .strokeWidth(2f)
            .strokeColor(Color.parseColor("#4CAF50"))
            .fillColor(Color.parseColor("#224CAF50")))
    }

    private fun updateMapMarkers(hives: List<Hive>) {
        if (!::mMap.isInitialized) return
        
        // Find markers to remove
        val currentHiveIds = hives.map { it.id }.toSet()
        val idsToRemove = hiveMarkers.keys.filter { it !in currentHiveIds }
        idsToRemove.forEach { id ->
            hiveMarkers[id]?.remove()
            hiveMarkers.remove(id)
            Log.d(TAG, "updateMapMarkers: Removed stale marker for hive $id")
        }

        // Add or update markers
        hives.forEach { hive ->
            val pos = LatLng(hive.latitude, hive.longitude)
            if (hiveMarkers.containsKey(hive.id)) {
                // Update existing marker position if changed
                hiveMarkers[hive.id]?.position = pos
            } else {
                // Create new marker
                val marker = mMap.addMarker(MarkerOptions()
                    .position(pos)
                    .title(hive.name)
                    .snippet("Status: ${hive.status}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                if (marker != null) {
                    hiveMarkers[hive.id] = marker
                    Log.d(TAG, "updateMapMarkers: Added NEW marker for hive: ${hive.name}")
                }
            }
        }
    }

    private fun showPinDialog(latLng: LatLng) {
        Log.d(TAG, "showPinDialog: Prompting for location confirmation")
        // Temporary feedback marker
        val tempMarker = mMap.addMarker(MarkerOptions()
            .position(latLng)
            .title("New Hive Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Pin Location")
            .setMessage("Do you want to save a hive at this coordinates?\nLat: ${latLng.latitude}\nLng: ${latLng.longitude}")
            .setPositiveButton("Yes, Pin It") { _, _ ->
                val intent = Intent(this, AddHiveActivity::class.java)
                intent.putExtra("LAT", latLng.latitude)
                intent.putExtra("LNG", latLng.longitude)
                startActivity(intent)
                tempMarker?.remove()
            }
            .setNegativeButton("No") { _, _ ->
                tempMarker?.remove()
            }
            .setOnCancelListener {
                tempMarker?.remove()
            }
            .show()
    }
}
