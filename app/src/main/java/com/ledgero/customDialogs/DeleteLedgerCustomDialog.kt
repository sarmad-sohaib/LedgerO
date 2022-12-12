package com.ledgero.customDialogs
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import com.ledgero.R

import com.ledgero.databinding.DeleteLedgerCustomDialogBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


private const val TAG= "DeleteLedgerDialog"
class DeleteLedgerCustomDialog(
    private val ledgerUID: String,
    private val friendName: String,
    private val friendUID: String,
    private val isLedgerDeleted: (Boolean) -> Unit,
    private val mContext:Context
) : DialogFragment() {
    private lateinit var _binding: DeleteLedgerCustomDialogBinding
    private val binding get() = _binding
    private var dbReference = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DeleteLedgerCustomDialogBinding.inflate(inflater, container, false)


        binding.titleDeleteLedger.text = friendName+getString(R.string.friend_makes_a_request_to_delete_this_ledger)

        binding.btnAcceptLedgerDelete.setOnClickListener{
            showProgress()
            deleteLedger()
        }

        binding.btCancelLedgerDelete.setOnClickListener {
            dbReference.child("deleteLedgerRequests").child(ledgerUID).setValue(null).addOnCompleteListener {
               dismiss()
            }
        }


        return binding.root
    }

    private fun showProgress() {
        binding.progressDeleteLedger.visibility=View.VISIBLE
        binding.textDeleteLedger.visibility= View.VISIBLE
    }
    private fun hideProgress(){
        binding.progressDeleteLedger.visibility=View.GONE
        binding.textDeleteLedger.visibility= View.GONE
    }

    private fun deleteLedger() {

        GlobalScope.launch {
           val a= async {
                var ledgerIndex=-1;
                var found = false
                for(i in 0 until User.getUserSingleLedgers()!!.size){

                    if (User.getUserSingleLedgers()!![i].ledgerUID== ledgerUID) {ledgerIndex=i

                    found=true
                    }
                }
                if (found){

                    User.getUserSingleLedgers()!!.removeAt(ledgerIndex)
                    User
                    dbReference.child("users").child(User.userID!!).child("user_single_Ledgers")
                        .setValue(User.getUserSingleLedgers()!!).addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(mContext, "Ledger deleted from your account", Toast.LENGTH_SHORT).show()

                            }else{
                                Toast.makeText(mContext, "Could Not delete ledger. Please try again later", Toast.LENGTH_SHORT).show()
                            }
                        }
                }else{

                    Toast.makeText(mContext, "Could not find ledger you want to delete", Toast.LENGTH_SHORT).show()
                }




            }

           val b= async {

                dbReference.child("users").child(friendUID).child("user_single_Ledgers").get()
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            if (it.result.exists()){
                            val friendLedgers= ArrayList<SingleLedgers>()

                                it.result.children.forEach{
                                     val ledger= it.getValue(SingleLedgers::class.java)
                                        friendLedgers.add(ledger!!)


                                }

                                var ledgerIndex=-1;
                                var found = false
                                for(i in 0 until friendLedgers.size){

                                    if (friendLedgers[i].ledgerUID== ledgerUID) {ledgerIndex=i

                                        found=true
                                    }
                                }
                                if (found){

                                    friendLedgers.removeAt(ledgerIndex)
                                    dbReference.child("users").child(friendUID).child("user_single_Ledgers")
                                        .setValue(friendLedgers).addOnCompleteListener {
                                            if (it.isSuccessful){
                                                Log.d(TAG, "deleteLedger: Ledger deleted from your friends account")
                                            }else{
                                                Log.d(TAG, "deleteLedger: could not delete from friend ledger")

                                            }
                                        }
                                }else{

                                    Log.d(TAG, "deleteLedger: could not delete from friend ledger")


                                }





                            }
                        }
                    }





            }

           val c= async {
                dbReference.child("ledgerInfo").child(ledgerUID).setValue(null)
                dbReference.child("ledgerEntries").child(ledgerUID).setValue(null)
                dbReference.child("canceledEntries").child(ledgerUID).setValue(null)
                dbReference.child("entriesRequests").child(ledgerUID).setValue(null)
                dbReference.child("deleteLedgerRequests").child(ledgerUID).setValue(null)

            }
            a.await()
            b.await()
            c.await()

            hideProgress()
            dismiss()
            isLedgerDeleted(true)
        }

    }


}