package com.example.currencyrate.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.databinding.ItemCurrencyCompactBinding
import com.example.currencyrate.databinding.ItemCurrencyGlassBinding
import java.util.Locale

class CurrencyAdapter(
    private val onFavoriteClick: (String, Boolean) -> Unit,
    private val onItemClick: (CurrencyEntity) -> Unit
) : ListAdapter<CurrencyEntity, CurrencyAdapter.BaseViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_GLASS = 1
        private const val TYPE_COMPACT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isFavorite) TYPE_GLASS else TYPE_COMPACT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_GLASS -> {
                val binding = ItemCurrencyGlassBinding.inflate(inflater, parent, false)
                GlassViewHolder(binding)
            }
            else -> {
                val binding = ItemCurrencyCompactBinding.inflate(inflater, parent, false)
                CompactViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    abstract inner class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: CurrencyEntity)

        protected fun setupCommonUI(
            item: CurrencyEntity,
            tvCode: android.widget.TextView,
            tvName: android.widget.TextView,
            tvValue: android.widget.TextView,
            ivFavorite: android.widget.ImageView
        ) {
            tvCode.text = item.code
            tvName.text = item.name
            tvValue.text = String.format(Locale.getDefault(), "%.2f", item.rate)

            val starIcon = if (item.isFavorite) {
                android.R.drawable.btn_star_big_on
            } else {
                android.R.drawable.btn_star_big_off
            }
            ivFavorite.setImageResource(starIcon)

            ivFavorite.setOnClickListener {
                onFavoriteClick(item.code, !item.isFavorite)
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    inner class GlassViewHolder(private val binding: ItemCurrencyGlassBinding) : BaseViewHolder(binding) {
        override fun bind(item: CurrencyEntity) {
            setupCommonUI(item, binding.tvCode, binding.tvName, binding.tvValue, binding.ivFavorite)
            binding.tvTrend.text = "+0.15%" // Заглушка
        }
    }

    inner class CompactViewHolder(private val binding: ItemCurrencyCompactBinding) : BaseViewHolder(binding) {
        override fun bind(item: CurrencyEntity) {
            setupCommonUI(item, binding.tvCode, binding.tvName, binding.tvValue, binding.ivFavorite)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CurrencyEntity>() {
        override fun areItemsTheSame(oldItem: CurrencyEntity, newItem: CurrencyEntity) = oldItem.code == newItem.code
        override fun areContentsTheSame(oldItem: CurrencyEntity, newItem: CurrencyEntity) = oldItem == newItem
    }
}
