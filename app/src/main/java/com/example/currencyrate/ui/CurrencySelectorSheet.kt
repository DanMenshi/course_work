package com.example.currencyrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.databinding.LayoutBottomSheetAddBinding
import com.example.currencyrate.viewmodel.ConverterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CurrencySelectorSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConverterViewModel by activityViewModels()

    private var selectionType: SelectionType = SelectionType.GIVE

    enum class SelectionType { GIVE, RECEIVE }

    companion object {
        private const val ARG_SELECTION_TYPE = "selection_type"

        fun newInstance(type: SelectionType): CurrencySelectorSheet {
            val fragment = CurrencySelectorSheet()
            val args = Bundle()
            args.putSerializable(ARG_SELECTION_TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectionType = arguments?.getSerializable(ARG_SELECTION_TYPE) as? SelectionType ?: SelectionType.GIVE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutBottomSheetAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CurrencyAdapter(
            onFavoriteClick = { _, _ -> },
            onItemClick = { currency ->
                if (selectionType == SelectionType.GIVE) {
                    viewModel.updateGiveCurrency(currency)
                } else {
                    viewModel.updateReceiveCurrency(currency)
                }
                dismiss()
            }
        )

        binding.rvAllCurrencies.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.allCurrencies.observe(viewLifecycleOwner) { currencies ->
            val list = currencies.toMutableList()
            if (list.none { it.code == "RUB" }) {
                list.add(0, CurrencyEntity("RUB", "R00000", "Российский рубль", 1.0, 1))
            }
            adapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
