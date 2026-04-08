package com.example.currencyrate.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.currencyrate.R
import com.example.currencyrate.data.local.AppDatabase
import com.example.currencyrate.data.remote.CbrApi
import com.example.currencyrate.data.repository.CurrencyRepository
import com.example.currencyrate.databinding.ActivityDetailsBinding
import com.example.currencyrate.viewmodel.DetailsViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.Locale

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    @Volatile
    private var currentHistoryDates: List<String> = emptyList()
    private var selectedDays = 7
    
    private val viewModel: DetailsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(applicationContext)
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://www.cbr.ru/")
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .build()
                val api = retrofit.create(CbrApi::class.java)
                val repository = CurrencyRepository(api, database.currencyDao())
                return DetailsViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val code = intent.getStringExtra("CURRENCY_CODE") ?: run {
            finish()
            return
        }
        val name = intent.getStringExtra("CURRENCY_NAME") ?: ""
        val rate = intent.getDoubleExtra("CURRENCY_RATE", 0.0)

        setupUI(code, name, rate)
        observeViewModel()

        viewModel.loadHistory(code, selectedDays)
    }

    private fun setupUI(code: String, name: String, rate: Double) {
        binding.btnBack.setOnClickListener { finish() }
        binding.tvCode.text = code
        binding.tvName.text = name

        val formattedRate = if (rate < 0.01) {
            String.format(Locale.getDefault(), "%.4f", rate)
        } else {
            String.format(Locale.getDefault(), "%.2f", rate)
        }
        binding.tvCurrentValue.text = "$formattedRate ₽"

        binding.tab7Days.setOnClickListener { updateTabs(7) }
        binding.tab30Days.setOnClickListener { updateTabs(30) }

        setupChart(binding.chart)
    }

    private fun updateTabs(days: Int) {
        if (days == selectedDays) return
        selectedDays = days

        binding.chart.clear()
        
        val code = binding.tvCode.text.toString()
        viewModel.loadHistory(code, days)
        
        val activeColor = ContextCompat.getColor(this, R.color.accent_blue)
        val inactiveColor = ContextCompat.getColor(this, R.color.text_secondary)
        
        if (days == 7) {
            binding.tab7Days.apply {
                setBackgroundResource(R.drawable.glass_card_bg)
                backgroundTintList = ColorStateList.valueOf(activeColor)
                setTextColor(Color.WHITE)
            }
            binding.tab30Days.apply {
                background = null
                setTextColor(inactiveColor)
            }
        } else {
            binding.tab30Days.apply {
                setBackgroundResource(R.drawable.glass_card_bg)
                backgroundTintList = ColorStateList.valueOf(activeColor)
                setTextColor(Color.WHITE)
            }
            binding.tab7Days.apply {
                background = null
                setTextColor(inactiveColor)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.history.observe(this) { history ->
            binding.chart.clear()
            
            if (history == null || history.isEmpty()) {
                currentHistoryDates = emptyList()
                return@observe
            }
            
            val newDates = history.map { it.date }
            currentHistoryDates = newDates
            
            val entries = history.mapIndexed { index, entity ->
                Entry(index.toFloat(), entity.rate.toFloat())
            }
            
            val marker = CustomMarkerView(this, R.layout.layout_chart_marker, newDates)
            binding.chart.marker = marker
            
            updateChartData(entries)
        }

        viewModel.stats.observe(this) { stats ->
            if (stats != null) {
                binding.tvMin.text = if (stats.first < 0.01) String.format(Locale.getDefault(), "%.4f", stats.first) else String.format(Locale.getDefault(), "%.2f", stats.first)
                binding.tvMax.text = if (stats.second < 0.01) String.format(Locale.getDefault(), "%.4f", stats.second) else String.format(Locale.getDefault(), "%.2f", stats.second)
            }
        }
    }

    private fun setupChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setExtraOffsets(10f, 10f, 10f, 20f)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.parseColor("#80FFFFFF")
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        val dates = currentHistoryDates
                        if (index >= 0 && index < dates.size) {
                            val rawDate = dates[index]
                            return try {
                                val parts = rawDate.split("-")
                                if (parts.size >= 3) "${parts[2]}.${parts[1]}" else rawDate
                            } catch (e: Exception) {
                                rawDate
                            }
                        }
                        return ""
                    }
                }
            }
            
            axisLeft.apply {
                textColor = Color.parseColor("#80FFFFFF")
                setDrawGridLines(true)
                gridColor = Color.parseColor("#1AFFFFFF")
                setDrawAxisLine(false)
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun updateChartData(entries: List<Entry>) {
        if (entries.isEmpty()) {
            binding.chart.clear()
            return
        }
        val dataSet = LineDataSet(entries, "Rate").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            color = ContextCompat.getColor(this@DetailsActivity, R.color.accent_blue)
            lineWidth = 3f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawHighlightIndicators(true)
            highLightColor = Color.WHITE
            highlightLineWidth = 1f
            
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@DetailsActivity, R.drawable.chart_gradient)
        }

        binding.chart.data = LineData(dataSet)
        binding.chart.invalidate()
        binding.chart.animateX(600)
    }
}
