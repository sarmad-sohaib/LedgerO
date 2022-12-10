package com.ledgero.more

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.ledgero.more.ui.MoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AppThemeDialog"

@AndroidEntryPoint
class AppThemeDialogFragment() : DialogFragment() {

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private val viewModel: MoreViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        var checkedItem = 0
        var currentCheckedItem = 0

        return activity?.let {
            val options = arrayOf("System Default", "Dark", "Light")
            val builder = AlertDialog.Builder(it)

            builder.setTitle("Choose theme")
                .setSingleChoiceItems(
                    options,
                    checkedItem
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
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        job.cancel()
    }
}