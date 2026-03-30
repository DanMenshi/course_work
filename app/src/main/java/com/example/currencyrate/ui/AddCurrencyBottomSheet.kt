package com.example.currencyrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyrate.databinding.LayoutBottomSheetAddBinding
import com.example.currencyrate.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddCurrencyBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetAddBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutBottomSheetAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = CurrencyAdapter(
            onFavoriteClick = { code, isFav -> viewModel.toggleFavorite(code, isFav) },
            onItemClick = { dismiss() }
        )
        
        binding.rvAllCurrencies.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.allCurrencies.observe(viewLifecycleOwner) { currencies ->
            adapter.submitList(currencies)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
