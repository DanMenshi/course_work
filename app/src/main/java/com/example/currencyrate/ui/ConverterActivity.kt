package com.example.currencyrate.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.currencyrate.data.local.AppDatabase
import com.example.currencyrate.data.local.CurrencyEntity
import com.example.currencyrate.data.remote.CbrApi
import com.example.currencyrate.data.repository.CurrencyRepository
import com.example.currencyrate.databinding.ActivityConverterBinding
import com.example.currencyrate.viewmodel.ConverterViewModel
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.Locale

class ConverterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConverterBinding
    
    private val viewModel: ConverterViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(applicationContext)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConverterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSwap.setOnClickListener {
            applySpringAnimation()
            viewModel.swapCurrencies()
        }

        binding.etAmountGive.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onInputChanged(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Открытие выбора валют (передаем строковый тип)
        binding.spinnerGive.setOnClickListener {
            CurrencySelectorSheet.newInstance("GIVE").show(supportFragmentManager, "select_give")
        }
        binding.spinnerReceive.setOnClickListener {
            CurrencySelectorSheet.newInstance("RECEIVE").show(supportFragmentManager, "select_receive")
        }

        // Слушаем ответ от шторки выбора
        supportFragmentManager.setFragmentResultListener(CurrencySelectorSheet.REQUEST_KEY, this) { _, bundle ->
            val selectedCode = bundle.getString("selected_code")
            val type = bundle.getString("type")
            if (selectedCode != null && type != null) {
                viewModel.setCurrencyByCode(selectedCode, type)
            }
        }
    }

    private fun applySpringAnimation() {
        val springAnim = SpringAnimation(binding.btnSwap, SpringAnimation.ROTATION, 180f)
        springAnim.spring.stiffness = SpringForce.STIFFNESS_LOW
        springAnim.spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        springAnim.start()
        
        springAnim.addEndListener { _, _, _, _ ->
            binding.btnSwap.rotation = 0f
        }
    }

    private fun observeViewModel() {
        viewModel.allCurrencies.observe(this) { currencies ->
            if (currencies.isNotEmpty()) {
                viewModel.setInitialCurrencies(currencies)
            }
        }

        viewModel.currencyGive.observe(this) { currency ->
            binding.tvCodeGive.text = currency?.code ?: ""
            binding.tvNameGive.text = currency?.name ?: ""
        }

        viewModel.currencyReceive.observe(this) { currency ->
            binding.tvCodeReceive.text = currency?.code ?: ""
            binding.tvNameReceive.text = currency?.name ?: ""
        }

        viewModel.resultAmount.observe(this) { amount ->
            binding.tvAmountReceive.text = String.format(Locale.getDefault(), "%.2f", amount)
        }

        viewModel.rateInfo.observe(this) { info ->
            binding.tvRateInfo.text = info
        }
    }
}
