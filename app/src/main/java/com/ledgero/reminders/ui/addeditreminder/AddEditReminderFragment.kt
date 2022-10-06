package com.ledgero.reminders.ui.addeditreminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ledgero.databinding.FragmentAddEditReminderBinding
import com.ledgero.reminders.reminders.data.Reminder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import com.ledgero.reminders.ui.addeditreminder.AddEditReminderFragmentEvents.*
import com.ledgero.utils.showSnackBar
import com.ledgero.utils.toDateTimeFormat
import java.text.DateFormat

@AndroidEntryPoint
class AddEditReminderFragment : Fragment() {

    private lateinit var mBinding: FragmentAddEditReminderBinding
    private val mViewModel: AddEditReminderViewModel by viewModels()
    private var mTimestamp = 0L

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentAddEditReminderBinding.inflate(inflater)

        mBinding.apply {

            if (mViewModel.reminder != null) {
                editTextReminderDescription.setText(mViewModel.reminderDescription)
                editTextReminderTime.setText(mViewModel.reminderTime.toDateTimeFormat())
                editTextReminderAmount.setText(mViewModel.reminderAmount)
                editTextReminderRecipient.setText(mViewModel.reminderRecipient)

                if (mViewModel.reminderIsGive) radioButtonReminderIsGive.isChecked = true
                else radioButtonReminderIsTake.isChecked = true

                buttonSaveReminder.text = "Update reminder"
                buttonSaveReminder.setOnClickListener {
                    mViewModel.onSave()
                }
            }

                editTextReminderTime.setOnClickListener {
                    pickDateTime()
                }

                editTextReminderAmount.addTextChangedListener {
                    mViewModel.reminderAmount = it.toString()
                }

                editTextReminderDescription.addTextChangedListener {
                    mViewModel.reminderDescription = it.toString()
                }

                editTextReminderRecipient.addTextChangedListener {
                    mViewModel.reminderRecipient = it.toString()
                }

                buttonSaveReminder.setOnClickListener {
                    mViewModel.saveReminderButtonClicked()
                }

                radioButtonReminderIsGive.setOnClickListener {
                    onRadioButtonClicked(radioButtonReminderIsGive)
                }

                radioButtonReminderIsTake.setOnClickListener {
                    onRadioButtonClicked(radioButtonReminderIsTake)
                }
            }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.uiEvent.collect { event ->
                    when(event) {
                        SaveReminderButtonClicked -> {
                            mViewModel.onSave()
                        }
                        is ShowInvalidInputMessage -> {
                            view?.showSnackBar(event.msg)
                        }
                        is NavigateBackOnSaveWithResult -> {
                            mBinding.apply {
                                editTextReminderTime.clearFocus()
                                editTextReminderRecipient.clearFocus()
                                editTextReminderAmount.clearFocus()
                                editTextReminderDescription.clearFocus()
                                radioGroupReminderIsGive.clearFocus()
                            }
                            setFragmentResult(
                                "save-result",
                                bundleOf("result" to event.result)
                            )
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }

        // Inflate the layout for this fragment
        return mBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun pickDateTime() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        val pickedDateTime = Calendar.getInstance()
                        pickedDateTime.set(year, month, day, hour, minute, 0)

                        mBinding.apply {
                            if (pickedDateTime.timeInMillis < System.currentTimeMillis()) {
                                editTextReminderTime.setText("Invalid time seleted! Try again")
                            } else {
                                editTextReminderTime.setText(DateFormat.getDateTimeInstance().format(pickedDateTime.timeInMillis))
                                mTimestamp = pickedDateTime.timeInMillis

                                mViewModel.reminderTime = mTimestamp
                            }
                        }
                    },
                    startHour,
                    startMinute,
                    false
                ).show()
            },
            startYear,
            startMonth,
            startDay
        ).show()
    }

    private fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when(view.id) {
                mBinding.radioButtonReminderIsGive.id -> {
                    if (checked) {
                        mViewModel.reminderIsGive = true
                    }
                }

                mBinding.radioButtonReminderIsTake.id -> {
                    if (checked) {
                        mViewModel.reminderIsGive = false
                    }
                }
            }
        }
    }
}