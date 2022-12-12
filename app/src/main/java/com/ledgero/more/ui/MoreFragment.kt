package com.ledgero.more.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ledgero.LoginActivity
import com.ledgero.databinding.FragmentMoreBinding
import com.ledgero.more.AppThemeDialogFragment
import com.ledgero.more.LanguageModelBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val TAG = "MoreFragment"

@AndroidEntryPoint
class MoreFragment : Fragment() {

    private lateinit var binding: FragmentMoreBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMoreBinding.inflate(inflater)
        val local = Locale.getDefault()

        binding.apply {

            textViewCurrentLanguage.text = Locale.getDefault().getDisplayLanguage(local)

            textViewChooseLanguage.setOnClickListener {
                showChooseThemeDialog()
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