package com.ledgero.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.ledgero.DataClasses.User
import com.ledgero.R
import com.ledgero.adapters.RecyclerViewAdapter
import com.ledgero.model.DatabaseUtill
import kotlinx.android.synthetic.main.fragment_custom_dialog.view.*

class CustomDialogFragment(var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?) : DialogFragment() {

var isUserAdded:Boolean= false


  override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        var rootView = inflater.inflate(R.layout.fragment_custom_dialog, container, false)
        var db= DatabaseUtill()

    rootView.bt_add_dialog_frag.setOnClickListener(){

        Toast.makeText(context, "Finding Users", Toast.LENGTH_SHORT).show()


        var email= rootView.tv_email_dialog_frag.text.toString()

        var user= db.getUser(email)
        if (user!=null){
            Toast.makeText(context, "$email : Found", Toast.LENGTH_SHORT).show()
            User.add_Single_Ledger(user)
            isUserAdded=true
            adapter!!.notifyDataSetChanged()


        }else{
            Toast.makeText(context, "$email : No Such User Found", Toast.LENGTH_SHORT).show()
        }

        }




        return rootView

    }}
