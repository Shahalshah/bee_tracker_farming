package com.example.ben.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.ben.databinding.FragmentMapBinding
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

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentCircle: Circle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        val mapFragment = childFragmentManager.findFragmentById(com.example.ben.R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }

        loadHives()

        mMap.setOnMapClickListener { latLng ->
            checkRoleAndShowPinDialog(latLng)
        }
    }

    private fun checkRoleAndShowPinDialog(latLng: LatLng) {
        val uid = FirebaseUtils.currentUserUid ?: return
        
        // Add a temporary blue marker
        val tempMarker = mMap.addMarker(MarkerOptions()
            .position(latLng)
            .title("New Hive?")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

        FirebaseUtils.usersRef().child(uid).child("role").get().addOnSuccessListener { snapshot ->
            if (snapshot.value == "Beekeeper") {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Add Hive")
                    .setMessage("Do you want to add a hive at this location?")
                    .setPositiveButton("Yes") { _, _ ->
                        val intent = android.content.Intent(requireContext(), com.example.ben.activities.AddHiveActivity::class.java)
                        intent.putExtra("LAT", latLng.latitude)
                        intent.putExtra("LNG", latLng.longitude)
                        startActivity(intent)
                    }
                    .setNegativeButton("No") { _, _ -> tempMarker?.remove() }
                    .setOnCancelListener { tempMarker?.remove() }
                    .show()
            } else {
                tempMarker?.remove()
                Toast.makeText(requireContext(), "Only beekeepers can pin hives", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            tempMarker?.remove()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                updateCircle(currentLatLng)
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

    private fun loadHives() {
        FirebaseUtils.hivesRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!::mMap.isInitialized) return
                mMap.clear()
                currentCircle?.center?.let { updateCircle(it) }

                for (hiveSnapshot in snapshot.children) {
                    val hive = hiveSnapshot.getValue(Hive::class.java)
                    hive?.let {
                        val pos = LatLng(it.latitude, it.longitude)
                        mMap.addMarker(MarkerOptions()
                            .position(pos)
                            .title(it.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
