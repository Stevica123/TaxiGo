package com.example.taxigo

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
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

    private lateinit var tipsViewPager: ViewPager2
    private lateinit var btnPrevTip: ImageButton
    private lateinit var btnNextTip: ImageButton
    private lateinit var tipsAdapter: TipsAdapter

    private val handler = Handler(Looper.getMainLooper())
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (::tipsAdapter.isInitialized && tipsAdapter.itemCount > 0) {
                val next = (tipsViewPager.currentItem + 1) % tipsAdapter.itemCount
                tipsViewPager.currentItem = next
                handler.postDelayed(this, 5000)
            }
        }
    }

    private val vehicleCountHandler = Handler(Looper.getMainLooper())
    private val vehicleCountRunnable = object : Runnable {
        override fun run() {
            availableVehiclesCount = (1..10).random()
            updateAvailableVehicles(availableVehiclesCount)
            vehicleCountHandler.postDelayed(this, 15000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("HomeFragment", "onCreateView povikan")

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        try {
            val ctx = requireContext().applicationContext
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            Log.d("HomeFragment", "OSMDroid configuration loaded")

            mapView = view.findViewById(R.id.mapView)
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.setMultiTouchControls(true)

            val mapController = mapView.controller
            mapController.setZoom(15.0)
            mapController.setCenter(GeoPoint(41.0311, 21.3409))
            Log.d("HomeFragment", "Map initialized")

            tvAvailableVehicles = view.findViewById(R.id.tvAvailableVehicles)
            updateAvailableVehicles(availableVehiclesCount)

            tipsViewPager = view.findViewById(R.id.tipsViewPager)
            btnPrevTip = view.findViewById(R.id.btnPrevTip)
            btnNextTip = view.findViewById(R.id.btnNextTip)

            val tipsList = requireContext().resources.getStringArray(R.array.tips_array).toList()
            tipsAdapter = TipsAdapter(tipsList)
            tipsViewPager.adapter = tipsAdapter
            Log.d("HomeFragment", "Tips initialized: ${tipsList.size} items")

            btnPrevTip.setOnClickListener {
                if (tipsAdapter.itemCount > 0) {
                    val prev = if (tipsViewPager.currentItem - 1 < 0) tipsAdapter.itemCount - 1 else tipsViewPager.currentItem - 1
                    tipsViewPager.currentItem = prev
                    Log.d("HomeFragment", "Previous tip clicked: $prev")
                }
            }

            btnNextTip.setOnClickListener {
                if (tipsAdapter.itemCount > 0) {
                    val next = (tipsViewPager.currentItem + 1) % tipsAdapter.itemCount
                    tipsViewPager.currentItem = next
                    Log.d("HomeFragment", "Next tip clicked: $next")
                }
            }

            val btnOrderTaxi = view.findViewById<Button>(R.id.btnOrderTaxi)
            btnOrderTaxi.setOnClickListener {
                Log.d("HomeFragment", "btnOrderTaxi clicked")
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
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error in onCreateView: ${e.localizedMessage}", e)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("HomeFragment", "onResume povikan")
        mapView.onResume()
        loadOrderMarkers()
        handler.postDelayed(autoScrollRunnable, 5000)
        vehicleCountHandler.postDelayed(vehicleCountRunnable, 5000)
    }

    override fun onPause() {
        super.onPause()
        Log.d("HomeFragment", "onPause povikan")
        mapView.onPause()
        handler.removeCallbacks(autoScrollRunnable)
        vehicleCountHandler.removeCallbacks(vehicleCountRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "onDestroyView povikan")
        mapView.onDetach()
    }

    private fun updateAvailableVehicles(count: Int) {
        Log.d("HomeFragment", "updateAvailableVehicles: $count")
        tvAvailableVehicles.text = count.toString()
    }

    private fun loadOrderMarkers() {
        Log.d("HomeFragment", "loadOrderMarkers povikan")
        val db = FirebaseFirestore.getInstance()

        db.collection("orders")
            .get()
            .addOnSuccessListener { result ->
                mapView.overlays.clear()
                Log.d("HomeFragment", "Orders fetched: ${result.size()} documents")

                for (document in result) {
                    val startLat = document.getDouble("latitude")
                    val startLon = document.getDouble("longitude")
                    val endLat = document.getDouble("endLatitude")
                    val endLon = document.getDouble("endLongitude")
                    val address = document.getString("address") ?: "Адреса непозната"

                    Log.d("HomeFragment", "Order: start=($startLat, $startLon), end=($endLat, $endLon), address=$address")

                    if (startLat != null && startLon != null) {
                        val startPoint = GeoPoint(startLat, startLon)
                        val startMarker = Marker(mapView).apply {
                            position = startPoint
                            title = "Почеток: $address"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(startMarker)
                    }

                    if (endLat != null && endLon != null) {
                        val endPoint = GeoPoint(endLat, endLon)
                        val endMarker = Marker(mapView).apply {
                            position = endPoint
                            title = "Крај: $address"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(endMarker)

                        if (startLat != null && startLon != null) {
                            val startPoint = GeoPoint(startLat, startLon)
                            val polyline = Polyline().apply {
                                setPoints(listOf(startPoint, endPoint))
                                color = Color.BLUE
                                width = 6f
                            }
                            mapView.overlays.add(polyline)
                        }
                    }
                }

                mapView.invalidate()
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error loading orders: ${e.localizedMessage}", e)
                Toast.makeText(requireContext(), "Грешка при вчитување нарачки", Toast.LENGTH_SHORT).show()
            }
    }

    inner class TipsAdapter(private val tips: List<String>) : RecyclerView.Adapter<TipsAdapter.TipViewHolder>() {

        inner class TipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTipText: TextView = itemView.findViewById(R.id.tvTipText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false)
            return TipViewHolder(view)
        }

        override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
            holder.tvTipText.text = tips[position]
            Log.d("HomeFragment", "Showing tip: ${tips[position]}")
        }

        override fun getItemCount(): Int = tips.size
    }
}
