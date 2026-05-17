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
    private var action: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        action = intent.getStringExtra("ACTION") ?: ""

        binding.toolbar.setNavigationOnClickListener { finish() }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }

        val defaultLocation = LatLng(12.9716, 77.5946)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f))

        // Add 2km radius circle around current location (or default)
        mMap.addCircle(CircleOptions()
            .center(defaultLocation)
            .radius(2000.0)
            .strokeWidth(2f)
            .strokeColor(Color.BLUE)
            .fillColor(0x220000FF))

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

    private fun loadHives() {
        FirebaseUtils.hivesRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Keep the circle, just clear markers if needed, but here I clear everything
                // So I redraw the circle too if I use mMap.clear()
                // Better approach: track markers or clear and redraw circle.
                mMap.clear()
                
                // Redraw circle
                val defaultLocation = LatLng(12.9716, 77.5946)
                mMap.addCircle(CircleOptions()
                    .center(defaultLocation)
                    .radius(2000.0)
                    .strokeWidth(2f)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF))

                for (hiveSnapshot in snapshot.children) {
                    val hive = hiveSnapshot.getValue(Hive::class.java)
                    hive?.let {
                        val pos = LatLng(it.latitude, it.longitude)
                        mMap.addMarker(MarkerOptions()
                            .position(pos)
                            .title(it.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapActivity, "Failed to load hives", Toast.LENGTH_SHORT).show()
            }
        })
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
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to pin hive: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
