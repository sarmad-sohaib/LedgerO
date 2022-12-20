package com.ledgero.more.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
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
import java.util.*

private const val TAG = "MoreFragment"

@AndroidEntryPoint
class MoreFragment : Fragment() {

    private lateinit var binding: FragmentMoreBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMoreBinding.inflate(inflater)
        val local = Locale.getDefault()

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