package com.example.currencyrate.ui

import android.content.Context
import android.widget.TextView
import com.example.currencyrate.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.util.Locale

class CustomMarkerView(context: Context, layoutResource: Int, private val dates: List<String>) : MarkerView(context, layoutResource) {

    private val tvMarkerDate: TextView = findViewById(R.id.tvMarkerDate)
    private val tvMarkerValue: TextView = findViewById(R.id.tvMarkerValue)

    override fun refreshContent(e: Entry, highlight: Highlight) {
        val index = e.x.toInt()
        if (index in dates.indices) {
            // Превращаем "2024-05-12" в "12.05"
            val rawDate = dates[index]
            val formattedDate = try {
                val parts = rawDate.split("-")
                "${parts[2]}.${parts[1]}"
            } catch (ex: Exception) {
                rawDate
            }
            tvMarkerDate.text = formattedDate
        }
        tvMarkerValue.text = String.format(Locale.US, "%.2f", e.y)
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}
