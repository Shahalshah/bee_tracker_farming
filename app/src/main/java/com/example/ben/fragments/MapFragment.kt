package com.example.ben.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ben.R
import com.example.ben.activities.AddHiveActivity
import com.example.ben.databinding.FragmentMapBinding
import com.example.ben.models.Hive
import com.example.ben.viewmodels.AuthViewModel
import com.example.ben.viewmodels.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private val mainViewModel: MainViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    
    private var userRole: String = ""
    private var currentLatLng: LatLng? = null
    private var currentCircle: Circle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        
        setupObservers()
    }

    private fun setupObservers() {
        authViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let { userRole = it.role }
        }

        mainViewModel.hives.observe(viewLifecycleOwner) { hives ->
            if (::mMap.isInitialized) {
                updateMapMarkers(hives)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }

        mainViewModel.fetchAllHives()

        mMap.setOnMapClickListener { latLng ->
            if (userRole == "Beekeeper") {
                showPinDialog(latLng)
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                currentLatLng = latLng
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                drawRadiusCircle(latLng)
            }
        }
    }

    private fun drawRadiusCircle(center: LatLng) {
        currentCircle?.remove()
        currentCircle = mMap.addCircle(CircleOptions()
            .center(center)
            .radius(2000.0)
            .strokeWidth(2f)
            .strokeColor(Color.BLUE)
            .fillColor(0x220000FF))
    }

    private fun updateMapMarkers(hives: List<Hive>) {
        mMap.clear()
        currentLatLng?.let { drawRadiusCircle(it) }

        hives.forEach { hive ->
            val pos = LatLng(hive.latitude, hive.longitude)
            mMap.addMarker(MarkerOptions()
                .position(pos)
                .title(hive.name)
                .snippet(hive.status)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
        }
    }

    private fun showPinDialog(latLng: LatLng) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Hive Location")
            .setMessage("Do you want to add a hive at this location?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(requireContext(), AddHiveActivity::class.java)
                intent.putExtra("LAT", latLng.latitude)
                intent.putExtra("LNG", latLng.longitude)
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
