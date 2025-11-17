package com.example.wangku.ui.home // Sesuaikan package Anda

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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

    // [PERBAIKAN] Atur formatter agar tidak menampilkan desimal
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    private val animationHandler = Handler(Looper.getMainLooper())
    private lateinit var shakeAnimation: Runnable

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
        setupLogoAnimation()
    }

    private fun setupLogoAnimation() {
        val shake = AnimationUtils.loadAnimation(context, R.anim.logo_shake)
        shake.repeatCount = Animation.INFINITE
        shakeAnimation = Runnable {
            binding.ivLogo.startAnimation(shake)
            animationHandler.postDelayed(shakeAnimation, 2000) // Ulangi setiap 2 detik
        }

        animationHandler.post(shakeAnimation) // Mulai animasi pertama kali
    }

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

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(currencyFormatter) { transaction ->
            showDeleteConfirmationDialog(transaction)
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus '${transaction.title}' senilai ${formatCurrency(transaction.amount)}?")
            .setPositiveButton("Hapus") { dialog, _ ->
                homeViewModel.delete(transaction)
                Toast.makeText(requireContext(), "Transaksi dihapus", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun observeViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.allTransactions.collect { dataItems ->

                if (dataItems.isEmpty()) {
                    binding.rvTransactions.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvTransactions.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                }

                transactionAdapter.submitList(dataItems)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.totalIncome.collect { income ->
                val incomeValue = income ?: 0.0
                binding.tvIncomeAmount.text = formatCurrency(incomeValue)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.totalExpense.collect { expense ->
                val expenseValue = expense ?: 0.0
                binding.tvExpenseAmount.text = "-${formatCurrency(expenseValue)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.totalBalance.collect { balance ->
                binding.tvTotalBalanceAmount.text = formatCurrency(balance)
            }
        }
    }

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
     * [PERBAIKAN] Fungsi helper untuk format mata uang tanpa .replace()
     */
    private fun formatCurrency(amount: Double): String {
        return currencyFormatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        animationHandler.removeCallbacks(shakeAnimation) // Hentikan animasi saat fragment dihancurkan
    }
}