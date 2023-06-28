package ru.home.swap.ui.offers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.home.swap.core.model.Service
import ru.home.swap.core.model.SwapMatch
import ru.home.swap.databinding.OffersViewItemBinding
import ru.home.swap.providers.PersonProvider
import kotlin.properties.Delegates

class OffersAdapter(
    val listener: IListener,
    diffCallback: DiffUtil.ItemCallback<SwapMatch> = OffersComparator()
) : PagingDataAdapter<SwapMatch, OffersAdapter.ViewHolder>(diffCallback) {

    interface IListener {
        fun onItemClick(item: SwapMatch)
    }

    class ViewHolder(internal val binding: OffersViewItemBinding)
        : RecyclerView.ViewHolder(binding.root)

    private var profileId: Long? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.callerId = profileId.toString()
        holder.binding.swapMatch = item!!
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

    fun setProfileId(id: Long?) {
        profileId = id
    }

    class OffersComparator: DiffUtil.ItemCallback<SwapMatch>() {
        override fun areItemsTheSame(oldItem: SwapMatch, newItem: SwapMatch): Boolean {
            return (oldItem.id == newItem.id)
                    && (oldItem.userFirstService.id == newItem.userFirstService.id)
                    && (oldItem.userSecondService.id == newItem.userSecondService.id)
        }

        override fun areContentsTheSame(oldItem: SwapMatch, newItem: SwapMatch): Boolean =
            oldItem == newItem

    }
}