package com.example.taxigo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter(
    private val orders: MutableList<Order>,
    private val onCancelClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVehicleType: TextView = view.findViewById(R.id.tvVehicleNumber)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvArrivalTime: TextView = view.findViewById(R.id.tvArrivalTime)
        val btnCancelOrder: Button = view.findViewById(R.id.btnCancelOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.tvVehicleType.text = "Тип на возило: ${order.vehicleNumber}"
        holder.tvAddress.text = "Адреса: ${order.address}"
        holder.tvArrivalTime.text = "Време на пристигнување: ${order.arrivalTime}"

        holder.btnCancelOrder.setOnClickListener {
            onCancelClick(order)
            removeOrderAt(position)
        }
    }

    override fun getItemCount(): Int = orders.size

    private fun removeOrderAt(position: Int) {
        orders.removeAt(position)
        notifyItemRemoved(position)
    }
}
