package com.ledgero.more.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.ledgero.DataClasses.User
import com.ledgero.LoginActivity
import com.ledgero.R
import com.ledgero.databinding.FragmentMoreBinding
import com.ledgero.model.UtillFunctions
import com.ledgero.more.AppThemeDialogFragment
import com.ledgero.more.LanguageModelBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "MoreFragment"

@AndroidEntryPoint
class MoreFragment : Fragment() {

    private lateinit var binding: FragmentMoreBinding
    private val moreViewModel: MoreViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMoreBinding.inflate(inflater)
        val local = Locale.getDefault()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                moreViewModel.prefFlow.collect { key ->
                    when (key) {
                        0 -> {
                            binding.textViewCurrentTheme.text = getString(R.string.system_default)
                        }
                        1 -> {
                            binding.textViewCurrentTheme.text = getString(R.string.dark_mode)
                        }
                        2 -> {
                            binding.textViewCurrentTheme.text = getString(R.string.light_mode)
                        }
                    }
                }
            }
        }

        binding.apply {

            textViewCurrentLanguage.text = Locale.getDefault().getDisplayLanguage(local)

            textViewChooseLanguage.setOnClickListener {
                showChooseLanguageBottomSheet()
            }

            logout.setOnClickListener {
                val auth: FirebaseAuth = Firebase.auth
                auth.signOut()
                User.signOut()
                for (i in 0 until parentFragmentManager.backStackEntryCount){
                    parentFragmentManager.popBackStack()
                }
                startActivity(Intent(requireContext(), LoginActivity::class.java))
               // activity?.finish()
            }

            textViewDarkMode.setOnClickListener {
                showChooseThemeDialog()
            }
        }
        binding.textViewEmail.setText(User.userEmail!!)


        binding.textViewUserName.setText(User.userName!!)

        binding.nameEditBtn.setOnClickListener{
            val builder = AlertDialog.Builder(requireContext())
            val inflater = layoutInflater
            builder.setTitle("Update Your User Name")
            val dialogLayout = inflater.inflate(R.layout.edit_user_name_dialog_layout, null)
            val editText  = dialogLayout.findViewById<EditText>(R.id.userNewName)
            builder.setView(dialogLayout)
            builder.setPositiveButton("OK") { dialogInterface, i -> updateUserName(editText.text.toString())}
            builder.show()
        }


        return binding.root
    }

    private fun updateUserName(userName: String) {


        if (!userName.isNullOrBlank()){
       val progress= UtillFunctions.setProgressDialog(requireContext(),"Updating Your Name")
            UtillFunctions.showProgressDialog(progress)
            FirebaseDatabase.getInstance().reference.child("users").child(User.userID!!).child("userName")
                .setValue(userName) .addOnCompleteListener {
                    User.userName= userName
                    binding.textViewUserName.setText(userName)
                    UtillFunctions.hideProgressDialog(progress)
                }

        }

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