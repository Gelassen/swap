package ru.home.swap.ui.offers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.home.swap.databinding.OffersViewItemBinding
import ru.home.swap.model.Service
import ru.home.swap.providers.PersonProvider

class OffersAdapter(val listener: IListener, diffCallback: DiffUtil.ItemCallback<Service> = OffersComparator())
    : PagingDataAdapter<Service, OffersAdapter.ViewHolder>(diffCallback) {

    interface IListener {
        fun onItemClick(item: Service)
    }

    class ViewHolder(internal val binding: OffersViewItemBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.service = item!!
        holder.binding.root.setOnClickListener {
            listener.onItemClick(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(
            OffersViewItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        )
        holder.binding.provider = PersonProvider()
        return holder
    }

    class OffersComparator: DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem == newItem

    }
}