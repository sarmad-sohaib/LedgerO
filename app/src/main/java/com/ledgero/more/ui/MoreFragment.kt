package com.ledgero.more.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ledgero.LoginActivity
import com.ledgero.databinding.FragmentMoreBinding
import com.ledgero.more.AppThemeDialogFragment
import com.ledgero.more.LanguageModelBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "MoreFragment"

@AndroidEntryPoint
class MoreFragment : Fragment() {

    private lateinit var binding: FragmentMoreBinding
    private lateinit var languageModelBottomSheet: LanguageModelBottomSheet
    private lateinit var appThemeDialogFragment: AppThemeDialogFragment
    private val viewModel: MoreViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        
        lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prefFlow.collect { key ->

                    Log.i(TAG, "onCreateView: $key")

                    when(key) {
                        0 -> {
                            setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                        }
                        1 -> {
                            setDefaultNightMode(MODE_NIGHT_YES)
                        }
                        2 -> {
                            setDefaultNightMode(MODE_NIGHT_NO)
                        }
                    }

                }
            }
        }


        binding = FragmentMoreBinding.inflate(inflater)
        languageModelBottomSheet = LanguageModelBottomSheet()
        appThemeDialogFragment = AppThemeDialogFragment()

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

            textViewDarkMode.setOnClickListener {
                appThemeDialogFragment.show(parentFragmentManager, "fa")
            }
        }

        return binding.root
    }
}