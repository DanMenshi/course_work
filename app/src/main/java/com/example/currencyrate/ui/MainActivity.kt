package com.example.currencyrate.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyrate.data.local.AppDatabase
import com.example.currencyrate.data.remote.CbrApi
import com.example.currencyrate.data.repository.CurrencyRepository
import com.example.currencyrate.databinding.ActivityMainBinding
import com.example.currencyrate.viewmodel.MainViewModel
import kotlinx.coroutines.launch
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
                    putExtra("CURRENCY_NAME", currency.name)
                    putExtra("CURRENCY_RATE", currency.rate)
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

        binding.fabAddCurrency.setOnClickListener {
            AddCurrencyBottomSheet().show(supportFragmentManager, "AddCurrencyBottomSheet")
        }

        binding.ivConvIcon.setOnClickListener {
            viewModel.refreshRates()
        }
    }

    private fun observeViewModel() {
        viewModel.favoriteCurrencies.observe(this) { currencies ->
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

        lifecycleScope.launch {
            viewModel.updateProgress.collect { progress ->
                if (progress == 0) {
                    binding.updateProgress.setProgressCompat(0, false)
                } else {
                    binding.updateProgress.setProgressCompat(progress, true)
                }
            }
        }
    }
}
