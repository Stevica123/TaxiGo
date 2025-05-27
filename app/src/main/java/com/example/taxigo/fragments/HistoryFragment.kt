package com.example.taxigo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
                    Toast.makeText(requireContext(), "Нарачката е откажана", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Грешка при откажување на нарачката", Toast.LENGTH_SHORT).show()
                }
        }

        recyclerView.adapter = adapter


        db.collection("orders")
            .get()
            .addOnSuccessListener { result ->
                orders.clear()
                for (document in result) {
                    val order = document.toObject(Order::class.java).copy(id = document.id)
                    orders.add(0, order)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Грешка при вчитување на нарачките", Toast.LENGTH_SHORT).show()
            }

        return view
    }
}
