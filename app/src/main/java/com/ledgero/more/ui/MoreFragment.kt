package com.ledgero.more.ui

import android.content.Intent
import android.icu.util.ULocale.getDisplayLanguage
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ledgero.LoginActivity
import com.ledgero.databinding.FragmentMoreBinding
import com.ledgero.more.LanguageModelBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val TAG = "MoreFragment"

@AndroidEntryPoint
class MoreFragment : Fragment() {

    private lateinit var binding: FragmentMoreBinding

    private val viewModel: MoreViewModel by viewModels()
    private lateinit var local: Locale
    private lateinit var languageModelBottomSheet: LanguageModelBottomSheet

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment


        binding = FragmentMoreBinding.inflate(inflater)
        languageModelBottomSheet = LanguageModelBottomSheet()

        val local = Locale.getDefault()

        binding.textViewCurrentLanguage.text = Locale.getDefault().getDisplayLanguage(local)

        binding.textViewChooseLanguage.setOnClickListener {
            languageModelBottomSheet.show(parentFragmentManager, LanguageModelBottomSheet.TAG)
        }


        binding.apply {
            logout.setOnClickListener {
                val auth: FirebaseAuth = Firebase.auth
                auth.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                activity?.finish()
            }
        }

        return binding.root
    }
}