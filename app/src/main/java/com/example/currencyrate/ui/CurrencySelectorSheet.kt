package com.example.currencyrate.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyrate.data.local.AppDatabase
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.databinding.LayoutBottomSheetSelectorBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CurrencySelectorSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetSelectorBinding? = null
    private val binding get() = _binding!!

    private var selectionType: String = "GIVE"
    private var allCurrencies: List<CurrencyEntity> = emptyList()

    companion object {
        private const val ARG_TYPE = "selection_type"
        const val REQUEST_KEY = "currency_selector_request"

        fun newInstance(type: String): CurrencySelectorSheet {
            return CurrencySelectorSheet().apply {
                arguments = bundleOf(ARG_TYPE to type)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutBottomSheetSelectorBinding.inflate(inflater, container, false)
        selectionType = arguments?.getString(ARG_TYPE) ?: "GIVE"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                sheet.setBackgroundColor(Color.TRANSPARENT)
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        val adapter = CurrencyAdapter(
            isSelectionMode = true,
            onFavoriteClick = { _, _ -> },
            onItemClick = { currency ->
                setFragmentResult(REQUEST_KEY, bundleOf(
                    "selected_code" to currency.code,
                    "type" to selectionType
                ))
                dismiss()
            }
        )

        binding.rvSelector.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        val dao = AppDatabase.getDatabase(requireContext()).currencyDao()
        dao.getAllCurrencies().asLiveData().observe(viewLifecycleOwner) { list ->
            val mutableList = list.toMutableList()
            if (mutableList.none { it.code == "RUB" }) {
                mutableList.add(0, CurrencyEntity("RUB", "R00000", "Российский рубль", 1.0, 1.0, 1))
            }
            allCurrencies = mutableList
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