package com.example.wangku.ui.categories // Sesuaikan package Anda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wangku.* // Import semua model, enum, dan Application class
import com.example.wangku.databinding.FragmentCategoriesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder // <-- Import Modern
import java.text.SimpleDateFormat
import java.util.* class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val categoriesViewModel: CategoriesViewModel by viewModels {
        CategoriesViewModelFactory((activity?.application as WangKuApplication).repository)
    }

    private lateinit var expenseAdapter: CategoryAdapter
    private lateinit var incomeAdapter: CategoryAdapter

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
    }

    /**
     * Menyiapkan data dummy dan menginisialisasi adapter
     */
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

        expenseAdapter = CategoryAdapter(expenseCategories) { category ->
            onCategoryClicked(category)
        }
        incomeAdapter = CategoryAdapter(incomeCategories) { category ->
            onCategoryClicked(category)
        }
    }

    /**
     * Menghubungkan RecyclerView di XML dengan Adapter dan LayoutManager.
     */
    private fun setupRecyclerViews() {
        binding.rvExpenseCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = expenseAdapter
            setHasFixedSize(true)
        }
        binding.rvIncomeCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = incomeAdapter
            setHasFixedSize(true)
        }
    }

    private fun onCategoryClicked(category: Category) {
        showAddTransactionDialog(category)
    }

    /**
     * Menampilkan dialog kustom (modern) untuk memasukkan jumlah uang.
     */
    private fun showAddTransactionDialog(category: Category) {
        // [PERBAIKAN UI] Gunakan MaterialAlertDialogBuilder
        val builder = MaterialAlertDialogBuilder(requireContext())
        val dialogTitle = if (category.type == CategoryType.EXPENSE) "Add Expense" else "Add Income"
        builder.setTitle("$dialogTitle: ${category.name}")

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null)
        builder.setView(dialogView)

        val amountEditText = dialogView.findViewById<EditText>(R.id.et_amount)

        builder.setPositiveButton("Save") { dialog, _ ->
            val amountString = amountEditText.text.toString()
            val amount = amountString.toDoubleOrNull()

            if (amount == null || amount <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            } else {
                saveTransaction(category, amount)
                dialog.dismiss()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    /**
     * Membuat objek Transaksi baru dan menyimpannya ke database
     * melalui ViewModel.
     */
    private fun saveTransaction(category: Category, amount: Double) {
        // Format tanggal ke "yyyy-MM-dd"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())

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

        // Buat objek Transaksi baru.
        // ID dan UserID akan diisi oleh Repository.
        val newTransaction = Transaction(
            title = category.name,
            date = currentDate,
            category = category.name,
            amount = amount,
            type = if (category.type == CategoryType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME,
            iconName = iconName
        )

        categoriesViewModel.insertTransaction(newTransaction)

        Toast.makeText(requireContext(), "Transaction Saved!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}