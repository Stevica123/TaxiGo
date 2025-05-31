package com.example.taxigo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.util.GeoPoint

class OrderFragment : Fragment() {


    private val streetCoordinates = mapOf(
        "Илинденска" to GeoPoint(41.0286, 21.3328),
        "Климент Охридски" to GeoPoint(41.0281, 21.3350),
        "Македонска" to GeoPoint(41.0265, 21.3305),
        "Никола Карев" to GeoPoint(41.0292, 21.3355),
        "Даме Груев" to GeoPoint(41.0279, 21.3322),
        "Мирче Ацев" to GeoPoint(41.0310, 21.3378),
        "Јордан Хаџи Константинов-Џинот" to GeoPoint(41.0252, 21.3317),
        "Питу Гули" to GeoPoint(41.0308, 21.3380)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order, container, false)

        val db = FirebaseFirestore.getInstance()

        val spinnerLocations = view.findViewById<Spinner>(R.id.spinnerLocations)


        val bitolaStreets = resources.getStringArray(R.array.bitola_streets).toList()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bitolaStreets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLocations.adapter = adapter

        val rgVehicleType = view.findViewById<RadioGroup>(R.id.rgVehicleType)
        val btnOrderTaxi = view.findViewById<Button>(R.id.btnOrderTaxi)

        btnOrderTaxi.setOnClickListener {

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(requireContext(), getString(R.string.please_login_to_order), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedPosition = spinnerLocations.selectedItemPosition
            if (selectedPosition == Spinner.INVALID_POSITION) {
                Toast.makeText(requireContext(), getString(R.string.please_select_location), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedStreet = spinnerLocations.selectedItem.toString()

            val selectedVehicleId = rgVehicleType.checkedRadioButtonId
            if (selectedVehicleId == -1) {
                Toast.makeText(requireContext(), getString(R.string.please_select_vehicle_type), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedVehicle = view.findViewById<RadioButton>(selectedVehicleId).text.toString()


            val selectedCoordinates = streetCoordinates[selectedStreet]

            val simulatedArrivalTime = (5..15).random()


            val orderData = hashMapOf(
                "vehicleNumber" to selectedVehicle,
                "address" to selectedStreet,
                "arrivalTime" to simulatedArrivalTime,
                "userId" to currentUser.uid,
                "latitude" to selectedCoordinates?.latitude,
                "longitude" to selectedCoordinates?.longitude
            )

            db.collection("orders")
                .add(orderData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), getString(R.string.order_successful), Toast.LENGTH_SHORT).show()

                    val bottomNavigationView =
                        requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
                    bottomNavigationView.selectedItemId = R.id.menu_history

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HistoryFragment())
                        .addToBackStack(null)
                        .commit()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), getString(R.string.order_error, e.message ?: ""), Toast.LENGTH_LONG).show()
                }
        }

        return view
    }
}
