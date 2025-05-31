package com.example.taxigo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private val orders = mutableListOf<Order>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = view.findViewById(R.id.rvOrders)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = OrderAdapter(orders) { orderToCancel ->

            db.collection("orders").document(orderToCancel.id)
                .delete()
                .addOnSuccessListener {
                    orders.remove(orderToCancel)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.order_cancelled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.order_cancel_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        recyclerView.adapter = adapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Ве молам најавете се.", Toast.LENGTH_SHORT).show()
            return view
        }

        val userId = currentUser.uid

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                orders.clear()
                for (document in result) {
                    val vehicleNumber = document.getString("vehicleNumber") ?: ""
                    val address = document.getString("address") ?: ""

                    val arrivalTimeAny = document.get("arrivalTime")
                    val arrivalTime = when (arrivalTimeAny) {
                        is Long -> arrivalTimeAny.toInt()
                        is String -> arrivalTimeAny.toIntOrNull() ?: 0
                        else -> 0
                    }

                    val order = Order(
                        id = document.id,
                        vehicleNumber = vehicleNumber,
                        address = address,
                        arrivalTime = arrivalTime
                    )
                    orders.add(0, order)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.order_load_error),
                    Toast.LENGTH_SHORT
                ).show()
            }

        return view
    }
}
