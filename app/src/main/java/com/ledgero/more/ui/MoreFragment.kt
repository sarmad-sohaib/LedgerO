package com.ledgero.more.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ledgero.LoginActivity
import com.ledgero.R
import com.ledgero.databinding.FragmentMoreBinding
import com.ledgero.more.AppThemeDialogFragment
import com.ledgero.more.LanguageModelBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "MoreFragment"

@AndroidEntryPoint
class MoreFragment : Fragment() {

    private lateinit var binding: FragmentMoreBinding
    private val moreViewModel: MoreViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMoreBinding.inflate(inflater)
        val local = Locale.getDefault()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                moreViewModel.prefFlow.collect { key ->
                    when (key) {
                        0 -> {
                            binding.textViewCurrentTheme.text = getString(R.string.system_default)
                        }
                        1 -> {
                            binding.textViewCurrentTheme.text = getString(R.string.dark_mode)
                        }
                        2 -> {
                            binding.textViewCurrentTheme.text = getString(R.string.light_mode)
                        }
                    }
                }
            }
        }

        binding.apply {

            textViewCurrentLanguage.text = Locale.getDefault().getDisplayLanguage(local)

            textViewChooseLanguage.setOnClickListener {
                showChooseLanguageBottomSheet()
            }

            logout.setOnClickListener {
                val auth: FirebaseAuth = Firebase.auth
                auth.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                activity?.finish()
            }

            textViewDarkMode.setOnClickListener {
                showChooseThemeDialog()
            }
        }
        return binding.root
    }

    private fun showChooseThemeDialog() {
        val appThemeDialogFragment = AppThemeDialogFragment()
        activity?.let { fragmentActivity ->
            appThemeDialogFragment.show(
                fragmentActivity.supportFragmentManager,
                AppThemeDialogFragment.TAG
            )
        }
    }

    private fun showChooseLanguageBottomSheet() {
        val languageModelBottomSheet = LanguageModelBottomSheet()
        activity?.let { fragmentActivity ->
            languageModelBottomSheet.show(
                fragmentActivity.supportFragmentManager,
                LanguageModelBottomSheet.TAG
            )
        }
    }
}