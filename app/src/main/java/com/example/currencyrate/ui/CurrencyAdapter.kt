package com.example.currencyrate.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.databinding.ItemCurrencyGlassBinding
import java.util.Locale

class CurrencyAdapter(
    private val onFavoriteClick: (String, Boolean) -> Unit,
    private val onItemClick: (CurrencyEntity) -> Unit
) : ListAdapter<CurrencyEntity, CurrencyAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCurrencyGlassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCurrencyGlassBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CurrencyEntity) {
            binding.tvCode.text = item.code
            binding.tvName.text = item.name
            binding.tvValue.text = String.format(Locale.getDefault(), "%.2f", item.rate)
            
            // Здесь можно добавить логику тренда (для примера пока статика)
            binding.tvTrend.text = "+0.15%" 

            val starIcon = if (item.isFavorite) {
                android.R.drawable.btn_star_big_on
            } else {
                android.R.drawable.btn_star_big_off
            }
            binding.ivFavorite.setImageResource(starIcon)

            binding.ivFavorite.setOnClickListener {
                onFavoriteClick(item.code, !item.isFavorite)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CurrencyEntity>() {
        override fun areItemsTheSame(oldItem: CurrencyEntity, newItem: CurrencyEntity) = oldItem.code == newItem.code
        override fun areContentsTheSame(oldItem: CurrencyEntity, newItem: CurrencyEntity) = oldItem == newItem
    }
}
