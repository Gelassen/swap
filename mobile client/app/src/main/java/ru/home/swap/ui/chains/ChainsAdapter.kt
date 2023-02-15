package ru.home.swap.ui.chains

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineDispatcher
import ru.home.swap.databinding.TxViewItemBinding
import ru.home.swap.wallet.model.ITransaction

class ChainsAdapter(
    val clickListener: ClickListener,
    diffCallback: DiffUtil.ItemCallback<ITransaction> = LawComparator(),
    mainDispatcher: CoroutineDispatcher,
    workerDispatcher: CoroutineDispatcher
) : PagingDataAdapter<ITransaction, ChainsAdapter.ViewHolder>(
    diffCallback,
    mainDispatcher,
    workerDispatcher
) {

    interface ClickListener {
        fun onItemClick(item: ITransaction)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tx = getItem(position)
        holder.binding.root.setOnClickListener { clickListener.onItemClick(holder.binding.tx!!) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TxViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    class ViewHolder(internal val binding: TxViewItemBinding) : RecyclerView.ViewHolder(binding.root)

    class LawComparator: DiffUtil.ItemCallback<ITransaction>() {
        override fun areItemsTheSame(oldItem: ITransaction, newItem: ITransaction): Boolean =
            oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: ITransaction, newItem: ITransaction): Boolean =
            oldItem.equals(newItem)

    }

}