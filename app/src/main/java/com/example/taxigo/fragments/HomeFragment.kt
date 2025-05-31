package com.example.taxigo

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class HomeFragment : Fragment() {

    private lateinit var tvAvailableVehicles: TextView
    private var availableVehiclesCount = 5

    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val ctx = requireContext().applicationContext
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        mapView = view.findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(15.0)
        mapController.setCenter(GeoPoint(41.0311, 21.3409))

        tvAvailableVehicles = view.findViewById(R.id.tvAvailableVehicles)
        updateAvailableVehicles(availableVehiclesCount)
        simulateVehicleCountChange()

        val btnOrderTaxi = view.findViewById<Button>(R.id.btnOrderTaxi)
        btnOrderTaxi.setOnClickListener {
            val orderFragment = OrderFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, orderFragment)
                .addToBackStack(null)
                .commit()

            val bottomNavigationView =
                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNavigationView.selectedItemId = R.id.menu_order
        }

        loadOrderMarkers()

        return view
    }

    private fun updateAvailableVehicles(count: Int) {
        tvAvailableVehicles.text = count.toString()
    }

    private fun simulateVehicleCountChange() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                availableVehiclesCount = (1..10).random()
                updateAvailableVehicles(availableVehiclesCount)
                handler.postDelayed(this, 15000)
            }
        }, 5000)
    }

    private fun loadOrderMarkers() {
        val db = FirebaseFirestore.getInstance()

        db.collection("orders")
            .get()
            .addOnSuccessListener { result ->
                mapView.overlays.clear()

                for (document in result) {
                    val startLat = document.getDouble("latitude")
                    val startLon = document.getDouble("longitude")
                    val endLat = document.getDouble("endLatitude")
                    val endLon = document.getDouble("endLongitude")
                    val address = document.getString("address") ?: "Адреса непозната"


                    if (startLat != null && startLon != null) {
                        val startPoint = GeoPoint(startLat, startLon)
                        val startMarker = Marker(mapView)
                        startMarker.position = startPoint
                        startMarker.title = "Почеток: $address"
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(startMarker)
                    }


                    if (endLat != null && endLon != null) {
                        val endPoint = GeoPoint(endLat, endLon)
                        val endMarker = Marker(mapView)
                        endMarker.position = endPoint
                        endMarker.title = "Крај: $address"
                        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(endMarker)


                        if (startLat != null && startLon != null) {
                            val startPoint = GeoPoint(startLat, startLon)
                            val polyline = Polyline()
                            polyline.setPoints(listOf(startPoint, endPoint))
                            polyline.color = Color.BLUE
                            polyline.width = 6f
                            mapView.overlays.add(polyline)
                        }
                    }
                }

                mapView.invalidate()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Грешка при вчитување нарачки", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        loadOrderMarkers()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
