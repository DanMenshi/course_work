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
    
    // Инициализация ViewModel (в реальном приложении лучше использовать DI, например Hilt)
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
        }
    }

    private fun setupListeners() {
        binding.btnOpenConverter.setOnClickListener {
            // Анимация нажатия в стиле iOS
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                startActivity(Intent(this, ConverterActivity::class.java))
            }.start()
        }
        
        binding.btnSettings.setOnClickListener {
            // Здесь будет открытие списка всех валют для добавления в избранное
        }
    }

    private fun observeViewModel() {
        viewModel.allCurrencies.observe(this) { currencies ->
            (binding.rvCurrencies.adapter as CurrencyAdapter).submitList(currencies)
        }
    }
}
