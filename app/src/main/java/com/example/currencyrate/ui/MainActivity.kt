package com.example.currencyrate.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
        val adapter = CurrencyAdapter(
            onFavoriteClick = { code, isFav -> viewModel.toggleFavorite(code, isFav) },
            onItemClick = { currency ->
                val intent = Intent(this, DetailsActivity::class.java).apply {
                    putExtra("CURRENCY_CODE", currency.code)
                }
                startActivity(intent)
            }
        )
        binding.rvCurrencies.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = adapter
            alpha = 0f
        }
    }

    private fun setupListeners() {
        binding.btnOpenConverter.setOnClickListener {
            startActivity(Intent(this, ConverterActivity::class.java))
        }
        
        binding.btnSettings.setOnClickListener {
            // Теперь все валюты на главном экране, но BottomSheet все еще полезен
            // для быстрого поиска или управления списком
            AddCurrencyBottomSheet().show(supportFragmentManager, "AddCurrencyBottomSheet")
        }
    }

    private fun observeViewModel() {
        /**
         * Подписываемся на ВЕСЬ список валют.
         * Благодаря сортировке в DAO (isFavorite DESC), избранные будут вверху.
         * Адаптер сам выберет нужный ViewType (Glass или Compact).
         */
        viewModel.allCurrencies.observe(this) { currencies ->
            (binding.rvCurrencies.adapter as CurrencyAdapter).submitList(currencies)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.pbLoading.isVisible = isLoading
            if (!isLoading) {
                binding.rvCurrencies.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start()
            }
        }

        viewModel.syncStatus.observe(this) { status ->
            binding.tvSubtitle.text = status
        }
    }
}
