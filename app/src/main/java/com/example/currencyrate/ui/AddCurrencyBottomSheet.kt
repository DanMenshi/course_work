package com.example.currencyrate.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.databinding.LayoutBottomSheetAddBinding
import com.example.currencyrate.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddCurrencyBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetAddBinding? = null
    private val binding get() = _binding!!
    
    private var allCurrencies: List<CurrencyEntity> = emptyList()

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutBottomSheetAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = CurrencyAdapter(
            onFavoriteClick = { code, isFav -> 
                viewModel.toggleFavorite(code, isFav) 
            },
            onItemClick = { currency ->
                dismiss()
            }
        )
        
        binding.rvAllCurrencies.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.allCurrencies.observe(viewLifecycleOwner) { currencies ->
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
