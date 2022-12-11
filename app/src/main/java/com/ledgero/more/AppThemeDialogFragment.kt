package com.ledgero.more

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.ledgero.more.ui.MoreViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AppThemeDialog"

@AndroidEntryPoint
class AppThemeDialogFragment : DialogFragment() {

    private val viewModel: MoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.keyFlow()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var checkeditem = 0
        var currentCheckedItem = 0

        val dialog = activity?.let {
            val options = arrayOf("System Default", "Dark", "Light")
            val builder = AlertDialog.Builder(it)

            builder.setTitle("Choose theme")
                .setSingleChoiceItems(
                    options,
                    viewModel.key
                ) { dialog, which ->

                    when (which) {
                        0 -> {
                            Toast.makeText(requireActivity(), "$which", Toast.LENGTH_SHORT).show()
                            currentCheckedItem = 0
                        }
                        1 -> {
                            Toast.makeText(requireActivity(), "$which", Toast.LENGTH_SHORT).show()
                            currentCheckedItem = 1
                        }
                        2 -> {
                            Toast.makeText(requireActivity(), "$which", Toast.LENGTH_SHORT).show()
                            currentCheckedItem = 2
                        }
                    }
                }
            builder.setPositiveButton("OK") { dialogInterface, i ->
                viewModel.update(currentCheckedItem)
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

        return dialog
    }
}