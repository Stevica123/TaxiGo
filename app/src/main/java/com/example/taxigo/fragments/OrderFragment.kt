package com.example.taxigo

import com.google.firebase.firestore.FirebaseFirestore
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

class OrderFragment : Fragment() {

    private val bitolaStreets = listOf(
        "Илинденска",
        "Климент Охридски",
        "Македонска",
        "Никола Карев",
        "Даме Груев",
        "Мирче Ацев",
        "Јордан Хаџи Константинов-Џинот",
        "Питу Гули"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order, container, false)

        val db = FirebaseFirestore.getInstance()

        val spinnerLocations = view.findViewById<Spinner>(R.id.spinnerLocations)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bitolaStreets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLocations.adapter = adapter

        val rgVehicleType = view.findViewById<RadioGroup>(R.id.rgVehicleType)
        val btnOrderTaxi = view.findViewById<Button>(R.id.btnOrderTaxi)

        btnOrderTaxi.setOnClickListener {
            val selectedPosition = spinnerLocations.selectedItemPosition
            if (selectedPosition == Spinner.INVALID_POSITION) {
                Toast.makeText(requireContext(), "Ве молам, изберете локација", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedStreet = spinnerLocations.selectedItem.toString()

            val selectedVehicleId = rgVehicleType.checkedRadioButtonId
            if (selectedVehicleId == -1) {
                Toast.makeText(requireContext(), "Ве молам, изберете тип на возило", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedVehicle = view.findViewById<RadioButton>(selectedVehicleId).text.toString()

            val simulatedArrivalTime = (5..15).random()
            val arrivalTimeStr = "$simulatedArrivalTime минути"

            val orderData = hashMapOf(
                "vehicleNumber" to selectedVehicle,
                "address" to selectedStreet,
                "arrivalTime" to arrivalTimeStr
            )

            db.collection("orders")
                .add(orderData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Нарачката е успешно испратена!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HistoryFragment())
                        .addToBackStack(null)
                        .commit()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Грешка при испраќање: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        return view
    }
}
