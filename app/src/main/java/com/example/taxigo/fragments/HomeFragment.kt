package com.example.taxigo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private lateinit var tvAvailableVehicles: TextView
    private var availableVehiclesCount = 5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvAvailableVehicles = view.findViewById(R.id.tvAvailableVehicles)
        updateAvailableVehicles(availableVehiclesCount)

        simulateVehicleCountChange()

        // Копче нарaчај
        val btnOrderTaxi = view.findViewById<Button>(R.id.btnOrderTaxi)
        btnOrderTaxi.setOnClickListener {
            // Отвори OrderFragment
            val orderFragment = OrderFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, orderFragment)  // Сменете го fragment_container со ID-то од вашиот layout
                .addToBackStack(null)
                .commit()
        }

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
}
