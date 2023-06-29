package ru.home.swap.ui.demands

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.home.swap.core.model.Service
import ru.home.swap.core.model.SwapMatch
import ru.home.swap.databinding.DemandsViewItemBinding
import ru.home.swap.providers.PersonProvider

class DemandsAdapter(
    val listener: IListener,
    diffCallback: DiffUtil.ItemCallback<Service> = DemandsComparator()
) : PagingDataAdapter<Service, DemandsAdapter.ViewHolder>(diffCallback) {

    interface IListener {
        fun onItemClick(item: Service)
    }

    class ViewHolder(internal val binding: DemandsViewItemBinding)
        : RecyclerView.ViewHolder(binding.root)

    private var profileId: Long? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.callerId = profileId.toString()
        holder.binding.service = item!!
        holder.binding.root.setOnClickListener {
            listener.onItemClick(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(
            DemandsViewItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        )
        holder.binding.provider = PersonProvider()
        return holder
    }

    fun setProfileId(id: Long?) {
        profileId = id
    }

    class DemandsComparator: DiffUtil.ItemCallback<Service>() {
        override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
            return (oldItem.id == newItem.id)
                    && (oldItem.id == newItem.id)
                    && (oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean =
            oldItem == newItem

    }
}