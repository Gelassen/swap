package ru.home.swap.ui.profile

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.home.swap.App
import ru.home.swap.databinding.ServiceViewItemBinding
import ru.home.swap.model.Service
import java.util.ArrayList

class ItemAdapter(val isOffers: Boolean): RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private var listener: Listener? = null

    private val model: MutableList<Service> = ArrayList()

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun update(model: List<Service>) {
        this.model.clear()
        Log.d(App.TAG, "[items] adapter models has ${model.count()} items")
        this.model.addAll(model)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ServiceViewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = model[position]
        holder.binding.service = item
        holder.binding.executePendingBindings()
        holder.binding.removeService.setOnClickListener {
            if (listener == null) return@setOnClickListener
            listener!!.onRemove(item, isOffers)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return model.count()
    }

    inner class ViewHolder(internal val binding: ServiceViewItemBinding)
        : RecyclerView.ViewHolder(binding.root)

    interface Listener {
        fun onRemove(item: Service, isOffers: Boolean)
    }
}