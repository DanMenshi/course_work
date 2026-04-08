package com.example.currencyrate.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyrate.data.local.AppDatabase
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.databinding.LayoutBottomSheetAddBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class AddCurrencyBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetAddBinding? = null
    private val binding get() = _binding!!

    private var allCurrencies: List<CurrencyEntity> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutBottomSheetAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. ГАРАНТИРОВАННО РАСТЯГИВАЕМ ШТОРКУ, ЧТОБЫ СПИСОК НЕ БЫЛ 0 ПИКСЕЛЕЙ
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT // Принудительная высота
                sheet.setBackgroundColor(Color.TRANSPARENT) // Убираем белый фон
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        val dao = AppDatabase.getDatabase(requireContext()).currencyDao()

        val adapter = CurrencyAdapter(
            isSelectionMode = false,
            onFavoriteClick = { code, isFav ->
                lifecycleScope.launch {
                    dao.updateFavoriteStatus(code, isFav)
                }
            },
            onItemClick = { }
        )

        binding.rvAllCurrencies.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        dao.getAllCurrencies().asLiveData().observe(viewLifecycleOwner) { currencies ->
            allCurrencies = currencies
            filterList(binding.etSearch.text.toString(), adapter)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString(), adapter)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterList(query: String, adapter: CurrencyAdapter) {
        val filtered = if (query.isEmpty()) {
            allCurrencies
        } else {
            allCurrencies.filter {
                it.code.contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}