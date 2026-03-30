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
    }

    private fun observeViewModel() {
        viewModel.currencyGive.observe(this) { currency ->
            binding.tvCodeGive.text = currency.code
            binding.tvNameGive.text = currency.name
        }

        viewModel.currencyReceive.observe(this) { currency ->
            binding.tvCodeReceive.text = currency.code
            binding.tvNameReceive.text = currency.name
        }

        viewModel.resultAmount.observe(this) { amount ->
            binding.tvAmountReceive.text = String.format(Locale.getDefault(), "%.2f", amount)
        }
    }

    private fun applySpringAnimation() {
        // Анимация вращения кнопки
        val rotationAnim = SpringAnimation(binding.btnSwap, SpringAnimation.ROTATION, binding.btnSwap.rotation + 180f)
        rotationAnim.spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        rotationAnim.spring.stiffness = SpringForce.STIFFNESS_LOW
        rotationAnim.start()

        // Анимация "встряски" карточек при свопе
        val shakeAnim1 = SpringAnimation(binding.cvGive, SpringAnimation.TRANSLATION_Y, 0f)
        shakeAnim1.setStartVelocity(1000f)
        shakeAnim1.spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
        shakeAnim1.start()

        val shakeAnim2 = SpringAnimation(binding.cvReceive, SpringAnimation.TRANSLATION_Y, 0f)
        shakeAnim2.setStartVelocity(-1000f)
        shakeAnim2.spring.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
        shakeAnim2.start()
    }
}
