package com.ledgero.groupLedger.customDialogs.addNewGroupDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.ledgero.databinding.DialogAddNewGroupBinding

private const val TAG="AddNewGroupDialog"
class AddNewGroupDialog : DialogFragment() {
    private lateinit var _binding: DialogAddNewGroupBinding
    private val binding get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding= DialogAddNewGroupBinding.inflate(inflater,container,false)

        return binding.root
    }

}