package com.example.currencyrate.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyrate.data.local.AppDatabase
import com.example.currencyrate.data.remote.CbrApi
import com.example.currencyrate.data.repository.CurrencyRepository
import com.example.currencyrate.databinding.LayoutBottomSheetAddBinding
import com.example.currencyrate.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class AddCurrencyBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetAddBinding? = null
    private val binding get() = _binding!!
    
    /**
     * Используем activityViewModels(), чтобы делиться данными с MainActivity.
     * Factory нужна только на случай, если ViewModel еще не была создана в Activity.
     */
    private val viewModel: MainViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(requireContext().applicationContext)
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://www.cbr.ru/")
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .build()
                val api = retrofit.create(CbrApi::class.java)
                val repository = CurrencyRepository(api, database.currencyDao())
                return MainViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutBottomSheetAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = CurrencyAdapter(
            onFavoriteClick = { code, isFav -> 
                Log.d("AddCurrencyBS", "Toggle favorite for $code to $isFav")
                viewModel.toggleFavorite(code, isFav) 
            },
            onItemClick = { currency ->
                dismiss() // Закрываем при выборе
            }
        )
        
        binding.rvAllCurrencies.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        // Наблюдаем за всеми валютами
        viewModel.allCurrencies.observe(viewLifecycleOwner) { currencies ->
            Log.d("AddCurrencyBS", "Observer triggered. List size: ${currencies.size}")
            if (currencies.isEmpty()) {
                Log.w("AddCurrencyBS", "Currencies list is empty, requesting refresh...")
                viewModel.refreshRates()
            }
            adapter.submitList(currencies)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
