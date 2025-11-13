package com.example.wangku.ui.home // Sesuaikan package Anda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wangku.FilterType
import com.example.wangku.R
import com.example.wangku.Transaction
import com.example.wangku.TransactionAdapter
import com.example.wangku.WangKuApplication
import com.example.wangku.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder // <-- Import Modern
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var firebaseAuth: FirebaseAuth

    private val homeViewModel: HomeViewModel by viewModels {
        val app = (activity?.application as WangKuApplication)
        HomeViewModelFactory(app.repository, app.firebaseAuth)
    }

    private val currencyFormatter: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserInfo()
        setupRecyclerView()
        observeViewModel()
        setupFilterChips()
    }

    /**
     * Mengambil info pengguna dari Firebase dan menampilkannya.
     */
    private fun setupUserInfo() {
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            val userName = currentUser.displayName
            if (!userName.isNullOrEmpty()) {
                binding.tvUsername.text = userName
            } else {
                binding.tvUsername.text = "User"
            }
        } else {
            binding.tvUsername.text = "Guest"
        }
    }

    /**
     * Menyiapkan RecyclerView dan meneruskan lambda untuk 'onLongClick'.
     */
    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan lambda untuk long-click
        transactionAdapter = TransactionAdapter { transaction ->
            // Saat item ditekan lama, panggil fungsi ini:
            showDeleteConfirmationDialog(transaction)
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    /**
     * Menampilkan dialog konfirmasi (modern) sebelum menghapus item.
     */
    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        // [PERBAIKAN UI] Gunakan MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus '${transaction.title}' senilai ${formatCurrency(transaction.amount)}?")
            .setPositiveButton("Hapus") { dialog, _ ->
                // Panggil ViewModel untuk menghapus
                homeViewModel.delete(transaction)
                Toast.makeText(requireContext(), "Transaksi dihapus", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    /**
     * "Mendengarkan" (observe) data dari ViewModel.
     * Termasuk logika untuk "Empty State".
     */
    private fun observeViewModel() {

        // 1. Observer untuk DAFTAR TRANSAKSI
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.allTransactions.collect { transactionsList ->

                // [LOGIKA EMPTY STATE BARU]
                if (transactionsList.isEmpty()) {
                    binding.rvTransactions.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvTransactions.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                }

                // Kirim daftar (walaupun kosong) ke adapter
                transactionAdapter.submitList(transactionsList)
            }
        }

        // 2. Observer untuk TOTAL INCOME
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.totalIncome.collect { income ->
                val incomeValue = income ?: 0.0
                binding.tvIncomeAmount.text = formatCurrency(incomeValue)
            }
        }

        // 3. Observer untuk TOTAL EXPENSE
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.totalExpense.collect { expense ->
                val expenseValue = expense ?: 0.0
                binding.tvExpenseAmount.text = "-${formatCurrency(expenseValue)}"
            }
        }

        // 4. Observer untuk TOTAL BALANCE
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.totalBalance.collect { balance ->
                binding.tvTotalBalanceAmount.text = formatCurrency(balance)
            }
        }
    }

    /**
     * Menambahkan listener ke ChipGroup untuk memfilter daftar.
     */
    private fun setupFilterChips() {
        binding.chipMonthly.isChecked = true

        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chip_daily -> {
                    homeViewModel.setFilter(FilterType.DAILY)
                }
                R.id.chip_weekly -> {
                    homeViewModel.setFilter(FilterType.WEEKLY)
                }
                R.id.chip_monthly -> {
                    homeViewModel.setFilter(FilterType.MONTHLY_ALL)
                }
            }
        }
    }

    /**
     * Fungsi helper untuk format mata uang
     */
    private fun formatCurrency(amount: Double): String {
        return currencyFormatter.format(amount).replace(",00", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}