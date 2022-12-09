package com.ledgero.more.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
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
import com.ledgero.data.preferences.Language
import com.ledgero.databinding.FragmentMoreBinding
import com.ledgero.more.LanguageModelBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "MoreFragment"

@AndroidEntryPoint
class MoreFragment : Fragment(), AdapterView.OnItemSelectedListener {

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
        binding.spinnerLanguage.onItemSelectedListener = this
        languageModelBottomSheet = LanguageModelBottomSheet()

        binding.textViewChooseLanguage.setOnClickListener {
            languageModelBottomSheet.show(parentFragmentManager, LanguageModelBottomSheet.TAG)
        }


        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.prefFlow.collect {
                    binding.spinnerLanguage.setSelection(it)
                }
            }
        }

        binding.apply {
            logout.setOnClickListener {
                val auth: FirebaseAuth = Firebase.auth
                auth.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                activity?.finish()
            }

            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.languages,
                android.R.layout.simple_spinner_item
            ).also { arrayAdapter ->
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerLanguage.adapter = arrayAdapter
            }

        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {
            if (parent.getItemAtPosition(position).toString().lowercase() == "urdu") {

            }
            if (parent.getItemAtPosition(position).toString().lowercase() == "english") {

            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}