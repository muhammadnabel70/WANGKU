package com.example.wangku.ui.analysis // Sesuaikan package Anda

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.wangku.AnalysisFilterType
import com.example.wangku.ChartDataEntry
import com.example.wangku.R
import com.example.wangku.WangKuApplication
import com.example.wangku.databinding.FragmentAnalysisBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AnalysisFragment : Fragment() {

    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!

    private val analysisViewModel: AnalysisViewModel by viewModels {
        val app = (activity?.application as WangKuApplication)
        AnalysisViewModelFactory(app.repository, app.firebaseAuth)
    }

    private val currencyFormatter: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        binding.headerLayout.tvHeaderTitle.text = "Analysis"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.analysisContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

        setupChartStyle()
        setupFilterChips()
        observeViewModel()
    }

    private fun setupFilterChips() {
        binding.chipMonthly.isChecked = true // Atur default

        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chip_daily -> {
                    analysisViewModel.setFilter(AnalysisFilterType.DAILY)
                }
                R.id.chip_weekly -> {
                    analysisViewModel.setFilter(AnalysisFilterType.WEEKLY)
                }
                R.id.chip_monthly -> {
                    analysisViewModel.setFilter(AnalysisFilterType.MONTHLY)
                }
                R.id.chip_yearly -> { // FIX: Changed from chip_year
                    analysisViewModel.setFilter(AnalysisFilterType.YEARLY)
                }
            }
        }
    }

    /**
     * "Mendengarkan" data dari ViewModel
     */
    private fun observeViewModel() {

        // --- Observer untuk Header DAN Total Bawah ---

        viewLifecycleOwner.lifecycleScope.launch {
            analysisViewModel.totalBalance.collect { balance ->
                binding.headerLayout.tvTotalBalanceAmount.text = formatCurrency(balance)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            analysisViewModel.totalExpense.collect { expense ->
                val expenseValue = expense ?: 0.0
                val formattedExpense = formatCurrency(expenseValue)

                binding.headerLayout.tvTotalExpenseAmount.text = "-${formattedExpense}"
                binding.tvTotalExpense.text = formattedExpense // FIX: Changed from tvBottomExpenseTotal
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            analysisViewModel.totalIncome.collect { income ->
                val incomeValue = income ?: 0.0
                binding.tvTotalIncome.text = formatCurrency(incomeValue) // FIX: Changed from tvBottomIncomeTotal
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            analysisViewModel.chartData.collect { data ->
                if (data.isNotEmpty()) {
                    updateGroupedBarChart(data)
                } else {
                    binding.barChart.clear()
                    binding.barChart.invalidate()
                }
            }
        }
    }

    private fun setupChartStyle() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)

            legend.isEnabled = true
            legend.textSize = 12f
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.BLACK
                textSize = 10f
                setCenterAxisLabels(true)
            }
            axisLeft.apply {
                setDrawGridLines(false)
                textColor = Color.DKGRAY
                textSize = 12f
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
        }
    }

    private fun updateGroupedBarChart(data: List<ChartDataEntry>) {

        val incomeEntries = ArrayList<BarEntry>()
        val expenseEntries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        data.forEachIndexed { index, summary ->
            incomeEntries.add(BarEntry(index.toFloat(), summary.totalIncome.toFloat()))
            expenseEntries.add(BarEntry(index.toFloat(), summary.totalExpense.toFloat()))
            labels.add(formatChartLabel(summary.label))
        }

        val incomeColor = ContextCompat.getColor(requireContext(), R.color.blue_income)
        val incomeDataSet = BarDataSet(incomeEntries, "Income")
        incomeDataSet.color = incomeColor

        val expenseColor = Color.RED
        val expenseDataSet = BarDataSet(expenseEntries, "Expense")
        expenseDataSet.color = expenseColor

        val groupSpace = 0.4f
        val barSpace = 0.05f
        val barWidth = 0.25f

        val barData = BarData(incomeDataSet, expenseDataSet)
        barData.barWidth = barWidth
        barData.setValueTextSize(10f)

        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChart.xAxis.axisMinimum = 0f
        binding.barChart.xAxis.axisMaximum = labels.size.toFloat()

        binding.barChart.data = barData
        binding.barChart.groupBars(0f, groupSpace, barSpace)
        binding.barChart.invalidate()
    }

    private fun formatChartLabel(dbLabel: String): String {
        return try {
            when {
                dbLabel.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> {
                    val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    displayFormat.format(dbFormat.parse(dbLabel)!!)
                }
                dbLabel.contains("-W") -> {
                    dbLabel.split("-W").last()
                }
                dbLabel.matches(Regex("^\\d{4}-\\d{2}$")) -> {
                    val dbFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("MMM yy", Locale.getDefault())
                    displayFormat.format(dbFormat.parse(dbLabel)!!)
                }
                else -> dbLabel
            }
        } catch (e: Exception) {
            dbLabel
        }
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormatter.format(amount).replace(",00", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}