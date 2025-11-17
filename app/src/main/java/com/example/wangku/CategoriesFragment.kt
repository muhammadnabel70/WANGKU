package com.example.wangku.ui.categories // Sesuaikan package Anda

import android.app.Dialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wangku.*
import com.example.wangku.databinding.FragmentCategoriesBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val categoriesViewModel: CategoriesViewModel by viewModels {
        val app = (activity?.application as WangKuApplication)
        CategoriesViewModelFactory(app.repository, FirebaseAuth.getInstance())
    }

    private lateinit var expenseAdapter: CategoryAdapter
    private lateinit var incomeAdapter: CategoryAdapter
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        binding.headerLayout.tvHeaderTitle.text = "Categories"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupRecyclerViews()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            categoriesViewModel.totalBalance.collect { balance ->
                binding.headerLayout.tvTotalBalanceAmount.text = formatCurrency(balance)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            categoriesViewModel.totalIncome.combine(categoriesViewModel.totalExpense) { income, expense ->
                Pair(income ?: 0.0, expense ?: 0.0)
            }.collect { (income, expense) ->
                updateHeaderUI(income, expense)
            }
        }
    }

    private fun updateHeaderUI(income: Double, expense: Double) {
        val formattedExpense = formatCurrency(expense)
        binding.headerLayout.tvTotalExpenseAmount.text = "-${formattedExpense}"

        val percentage = if (income > 0) {
            (expense / income * 100).toInt()
        } else {
            0
        }

        binding.headerLayout.progressBar.progress = percentage
        binding.headerLayout.tvProgressLabel.text = getIncomeSpendingMessage(percentage)
    }

    // [PERBAIKAN FINAL] Menggunakan logika pesan yang sama persis dengan AnalysisFragment
    private fun getIncomeSpendingMessage(percentage: Int): String {
         return "$percentage% Of Your Income, " + when {
            percentage > 100 -> "This is not good."
            percentage > 75  -> "Looks a bit high."
            percentage > 50  -> "Looks acceptable."
            else -> "Looks Good."
        }
    }

    private fun setupAdapters() {
        val expenseCategories = listOf(
            Category("cat_food", "Food", R.drawable.ic_makanan, CategoryType.EXPENSE),
            Category("cat_transport", "Transport", R.drawable.ic_transport, CategoryType.EXPENSE),
            Category("cat_medicine", "Medicine", R.drawable.ic_medicine, CategoryType.EXPENSE),
            Category("cat_groceries", "Groceries", R.drawable.ic_groceries, CategoryType.EXPENSE),
            Category("cat_rent", "Rent", R.drawable.ic_sewa, CategoryType.EXPENSE),
            Category("cat_others_exp", "Others", R.drawable.ic_others, CategoryType.EXPENSE)
        )

        val incomeCategories = listOf(
            Category("cat_savings", "Savings", R.drawable.ic_savings, CategoryType.INCOME),
            Category("cat_salary", "Salary", R.drawable.ic_salary, CategoryType.INCOME),
            Category("cat_others_inc", "Others", R.drawable.ic_others_income, CategoryType.INCOME)
        )

        expenseAdapter = CategoryAdapter(expenseCategories) { onCategoryClicked(it) }
        incomeAdapter = CategoryAdapter(incomeCategories) { onCategoryClicked(it) }
    }

    private fun setupRecyclerViews() {
        binding.rvExpenseCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = expenseAdapter
        }
        binding.rvIncomeCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = incomeAdapter
        }
    }

    private fun onCategoryClicked(category: Category) {
        showAddTransactionDialog(category)
    }

    private fun showAddTransactionDialog(category: Category) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_transaction)
        
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvDate = dialog.findViewById<TextView>(R.id.tv_date)
        val etAmount = dialog.findViewById<EditText>(R.id.et_amount)
        val tvTitleLabel = dialog.findViewById<TextView>(R.id.tv_title_label)
        val tvExpenseTitle = dialog.findViewById<TextView>(R.id.tv_expense_title)
        val etMessage = dialog.findViewById<EditText>(R.id.et_message)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)

        if (category.type == CategoryType.INCOME) {
            tvTitleLabel.text = "Income Title"
        } else {
            tvTitleLabel.text = "Expense Title"
        }

        tvExpenseTitle.text = category.name
        updateDateInView(tvDate)

        tvDate.setOnClickListener {
            showDatePickerDialog(tvDate)
        }

        etAmount.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    etAmount.removeTextChangedListener(this)
                    val cleanString = s.toString().replace(Regex("[^\\d]"), "")
                    val parsed = if (cleanString.isEmpty()) 0.0 else cleanString.toDouble()
                    val formatted = currencyFormatter.format(parsed)
                    current = formatted
                    etAmount.setText(formatted)
                    etAmount.setSelection(formatted.length)
                    etAmount.addTextChangedListener(this)
                }
            }
        })

        btnSave.setOnClickListener {
            val amountString = etAmount.text.toString().replace(Regex("[^\\d]"), "")
            val amount = amountString.toDoubleOrNull()
            val note = etMessage.text.toString()

            if (amount == null || amount <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            } else {
                saveTransaction(category, amount, note, calendar.time)
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDatePickerDialog(dateTextView: TextView) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView(dateTextView)
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView(dateTextView: TextView) {
        val format = "dd MMMM yyyy"
        val sdf = SimpleDateFormat(format, Locale.US)
        dateTextView.text = sdf.format(calendar.time)
    }

    private fun saveTransaction(category: Category, amount: Double, note: String, date: Date) {
        val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dbDateFormat.format(date)

        val iconName = when (category.id) {
            "cat_food" -> "ic_makanan"
            "cat_transport" -> "ic_transport"
            "cat_medicine" -> "ic_medicine"
            "cat_groceries" -> "ic_groceries"
            "cat_rent" -> "ic_sewa"
            "cat_savings" -> "ic_savings"
            "cat_salary" -> "ic_salary"
            "cat_others_inc" -> "ic_others_income"
            else -> "ic_others"
        }

        val newTransaction = Transaction(
            title = category.name,
            date = formattedDate,
            category = category.name,
            amount = amount,
            type = if (category.type == CategoryType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME,
            iconName = iconName,
            note = note
        )

        categoriesViewModel.insertTransaction(newTransaction)

        Toast.makeText(requireContext(), "Transaction Saved!", Toast.LENGTH_SHORT).show()
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}