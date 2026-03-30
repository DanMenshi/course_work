package com.example.currencyrate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyrate.data.local.AppDatabase
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.data.remote.CbrApi
import com.example.currencyrate.data.repository.CurrencyRepository
import com.example.currencyrate.databinding.LayoutBottomSheetAddBinding
import com.example.currencyrate.viewmodel.ConverterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class CurrencySelectorSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetAddBinding? = null
    private val binding get() = _binding!!

    // Используем ту же ViewModel, что и в ConverterActivity
    private val viewModel: ConverterViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(requireContext().applicationContext)
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://www.cbr.ru/")
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .build()
                val api = retrofit.create(CbrApi::class.java)
                val repository = CurrencyRepository(api, database.currencyDao())
                return ConverterViewModel(repository) as T
            }
        }
    }

    private var selectionType: SelectionType = SelectionType.GIVE

    enum class SelectionType { GIVE, RECEIVE }

    companion object {
        fun newInstance(type: SelectionType): CurrencySelectorSheet {
            val fragment = CurrencySelectorSheet()
            fragment.selectionType = type
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutBottomSheetAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CurrencyAdapter(
            onFavoriteClick = { _, _ -> /* В селекторе не меняем избранное */ },
            onItemClick = { currency ->
                if (selectionType == SelectionType.GIVE) {
                    viewModel.selectGiveCurrency(currency)
                } else {
                    viewModel.selectReceiveCurrency(currency)
                }
                dismiss()
            }
        )

        binding.rvAllCurrencies.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.allCurrencies.observe(viewLifecycleOwner) { currencies ->
            // Добавляем рубль в список для выбора, если его там нет
            val list = currencies.toMutableList()
            if (list.none { it.code == "RUB" }) {
                list.add(0, CurrencyEntity("RUB", "Российский рубль", 1.0, 1))
            }
            adapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
