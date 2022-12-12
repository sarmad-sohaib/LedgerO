package com.ledgero.more

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ledgero.more.ui.MoreViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppThemeDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "AppThemeDialog"
    }

    private val viewModel: MoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.keyFlow()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val themeOptions = arrayOf("System Default", "Dark", "Light")
            val themOptionsDialogBuilder = MaterialAlertDialogBuilder(requireContext())
            var selectedItem = viewModel.key

            themOptionsDialogBuilder.setTitle("Choose theme")
                .setSingleChoiceItems(
                    themeOptions,
                    selectedItem
                ) { _, which ->

                    selectedItem = which

                }.setPositiveButton("OK") { dialogInterface, _ ->
                    viewModel.update(selectedItem)
                    dialogInterface.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}