package com.example.currencyrate.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyrate.data.local.AppDatabase
import com.example.currencyrate.data.remote.CbrApi
import com.example.currencyrate.data.repository.CurrencyRepository
import com.example.currencyrate.databinding.ActivityMainBinding
import com.example.currencyrate.viewmodel.MainViewModel
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(applicationContext)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // На главном экране отображаем только избранные
        val adapter = CurrencyAdapter(
            onFavoriteClick = { code, isFav -> viewModel.toggleFavorite(code, isFav) },
            onItemClick = { currency ->
                // Переход к деталям (если нужно) или просто лог
            }
        )
        binding.rvCurrencies.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = adapter
        }
    }

    private fun setupListeners() {
        binding.btnOpenConverter.setOnClickListener {
            startActivity(Intent(this, ConverterActivity::class.java))
        }
        
        // Кнопка "+" (ic_add) открывает BottomSheet со всеми валютами
        binding.btnSettings.setOnClickListener {
            val bottomSheet = AddCurrencyBottomSheet()
            bottomSheet.show(supportFragmentManager, "AddCurrencyBottomSheet")
        }
    }

    private fun observeViewModel() {
        // Подписываемся на избранные для главного экрана
        viewModel.favoriteCurrencies.observe(this) { currencies ->
            (binding.rvCurrencies.adapter as CurrencyAdapter).submitList(currencies)
        }
    }
}
