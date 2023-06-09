package ru.home.swap.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.home.swap.core.model.Service
import ru.home.swap.databinding.ServiceViewItemBinding

class ItemAdapter(val isOffers: Boolean, val listener: Listener)
    : ListAdapter<Service, ItemAdapter.ViewHolder>(DiffCallback) {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = getItem(position)
        holder.binding.removeService.setOnClickListener {
            listener.onRemove(service, false)
        }
        holder.bind(service)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ServiceViewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    inner class ViewHolder(internal val binding: ServiceViewItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: Service) {
                binding.service = item
                binding.executePendingBindings()
                binding.removeService.setOnClickListener {
                    if (listener == null) return@setOnClickListener
                    listener.onRemove(item, isOffers)
                    notifyItemChanged(position)
                }
            }
        }

    companion object DiffCallback : DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(
            oldItem: Service,
            newItem: Service
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Service,
            newItem: Service
        ): Boolean {
            return oldItem == newItem
        }
    }

    interface Listener {
        fun onRemove(item: Service, isOffers: Boolean)
    }
/*    private var listener: Listener? = null

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
    }*/
}