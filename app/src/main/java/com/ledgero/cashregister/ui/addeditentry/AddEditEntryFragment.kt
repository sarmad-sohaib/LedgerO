package com.ledgero.cashregister.ui.addeditentry

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.ledgero.R
import com.ledgero.databinding.FragmentAddEditEntryBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.android.synthetic.main.fragment_entries.*
import java.text.DateFormat

private const val TAG = "AddEditEntryFragment"

@AndroidEntryPoint
class AddEditEntryFragment : Fragment() {

    private lateinit var binding: FragmentAddEditEntryBinding
    private val viewModel: AddEditEntryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAddEditEntryBinding.inflate(inflater)

        val currentTimeStamp = System.currentTimeMillis()

        binding.apply {
            if (viewModel.entry != null) {
                editTextEntryAmount.setText(viewModel.entryAmount)
                editTextEntryDescription.setText(viewModel.entryDescription)
                textViewTimeStamp.text =
                    DateFormat.getDateTimeInstance().format(viewModel.entry?.dateTimeStamp)

                buttonSaveEntry.setOnClickListener{
                    viewModel.entry!!.out?.let { isCashOut -> viewModel.onSaveClick(isCashOut) }
                }
            }

            binding.textViewTimeStamp.text = DateFormat.getDateTimeInstance().format(currentTimeStamp)

            editTextEntryAmount.addTextChangedListener {
                viewModel.entryAmount = it.toString()
            }

            editTextEntryDescription.addTextChangedListener {
                viewModel.entryDescription = it.toString()
            }
        }

        val args: AddEditEntryFragmentArgs by navArgs()

        //execute after cash in button is clicked in EntriesFragment.kt
        if (args.cashStatus == "in") {

            binding.apply {
                buttonSaveEntry.setOnClickListener {
                    viewModel.onSaveClick(isCashOut = false)
                }
            }
        }

        //execute after cash out button is clicked in EntriesFragment.kt
        if (args.cashStatus == "out") {

            binding.apply {
                buttonSaveEntry.setOnClickListener {
                    viewModel.onSaveClick(isCashOut = true)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditEntryEvent.collect { event ->
                when(event) {
                    is AddEditEntryEvents.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is AddEditEntryEvents.NavigateBackWithResult -> {
                        binding.apply {
                            editTextEntryAmount.clearFocus()
                            editTextEntryDescription.clearFocus()

                            setFragmentResult(
                                "add_edit_request",
                                bundleOf("add_edit_result" to event.result)
                            )
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }
}