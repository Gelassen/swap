package ru.home.swap.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.home.swap.databinding.OfferViewItemBinding
import ru.home.swap.model.PersonView
import java.util.*

class MainAdapter: RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    inner class ViewHolder(internal val binding: OfferViewItemBinding)
        : RecyclerView.ViewHolder(binding.root)

    private val model: MutableList<PersonView> = mutableListOf()

    fun update(newData: List<PersonView>) {
        model.clear()
        model.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OfferViewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = model[position]
        holder.binding.person = item
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return model.size
    }
}