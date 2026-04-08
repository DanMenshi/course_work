package com.example.currencyrate.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.currencyrate.R
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.databinding.ItemCurrencyCompactBinding
import com.example.currencyrate.databinding.ItemCurrencyGlassBinding
import java.util.Locale

class CurrencyAdapter(
    private val isSelectionMode: Boolean = false,
    private val onFavoriteClick: (String, Boolean) -> Unit,
    private val onItemClick: (CurrencyEntity) -> Unit
) : ListAdapter<CurrencyEntity, CurrencyAdapter.BaseViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_GLASS = 1
        private const val TYPE_COMPACT = 2
    }

    override fun getItemViewType(position: Int): Int {
        if (isSelectionMode) return TYPE_COMPACT
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
            tvValue.text = if (item.rate < 0.01) {
                String.format(Locale.getDefault(), "%.4f", item.rate)
            } else {
                String.format(Locale.getDefault(), "%.2f", item.rate)
            }

            if (isSelectionMode) {
                ivFavorite.visibility = View.GONE
            } else {
                ivFavorite.visibility = View.VISIBLE
                updateStarUI(item.isFavorite, ivFavorite)

                ivFavorite.setOnClickListener {
                    val newState = !item.isFavorite

                    updateStarUI(newState, ivFavorite)

                    ivFavorite.animate()
                        .scaleX(0.5f).scaleY(0.5f).rotationBy(-30f)
                        .setDuration(150)
                        .withEndAction {
                            ivFavorite.animate()
                                .scaleX(1.3f).scaleY(1.3f).rotationBy(60f)
                                .setDuration(150)
                                .withEndAction {
                                    ivFavorite.animate()
                                        .scaleX(1f).scaleY(1f).rotationBy(-30f)
                                        .setDuration(300)
                                        .setInterpolator(OvershootInterpolator(2f))
                                        .start()
                                }.start()
                        }.start()

                    onFavoriteClick(item.code, newState)
                }
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun updateStarUI(isFavorite: Boolean, ivFavorite: android.widget.ImageView) {
            if (isFavorite) {
                ivFavorite.setImageResource(android.R.drawable.btn_star_big_on)
                ivFavorite.setColorFilter(ContextCompat.getColor(itemView.context, R.color.accent_blue))
            } else {
                ivFavorite.setImageResource(android.R.drawable.btn_star_big_off)
                ivFavorite.setColorFilter(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            }
        }
    }

    inner class GlassViewHolder(private val binding: ItemCurrencyGlassBinding) : BaseViewHolder(binding) {
        override fun bind(item: CurrencyEntity) {
            setupCommonUI(item, binding.tvCode, binding.tvName, binding.tvValue, binding.ivFavorite)


            val diff = item.rate - item.previousRate
            val percent = if (item.previousRate != 0.0) (diff / item.previousRate) * 100 else 0.0

            if (diff >= 0) {
                binding.tvTrend.text = String.format(Locale.US, "+%.2f%%", percent)
                binding.tvTrend.setTextColor(ContextCompat.getColor(itemView.context, R.color.green_up))
            } else {
                binding.tvTrend.text = String.format(Locale.US, "%.2f%%", percent)
                binding.tvTrend.setTextColor(ContextCompat.getColor(itemView.context, R.color.red_down))
            }
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