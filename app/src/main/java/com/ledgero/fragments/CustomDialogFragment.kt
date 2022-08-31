package com.ledgero.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.ledgero.DataClasses.User
import com.ledgero.Interfaces.FetchUsers
import com.ledgero.R
import com.ledgero.model.DatabaseUtill
import com.ledgero.model.UtillFunctions
import kotlinx.android.synthetic.main.fragment_custom_dialog.view.*


class CustomDialogFragment(var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?) : DialogFragment() {

var isUserAdded:Boolean= false
 lateinit var alertDialog: AlertDialog
 lateinit var infoTextView:TextView
 lateinit var userFoundList_Container: RelativeLayout
 lateinit var userFoundList_userName: TextView
 lateinit var userFoundList_userEmail: TextView
 lateinit var userFoundList_addFriendBtn: Button
 lateinit var friendUserUID: String
 lateinit var friendUserName: String
 lateinit var friendUserEmail: String



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        var rootView = inflater.inflate(R.layout.fragment_custom_dialog, container, false)
        var db= DatabaseUtill()

        //binding Views
        infoTextView=rootView.findViewById(R.id.info_tv_dialog_frag);
        userFoundList_Container=rootView.findViewById(R.id.userFound_Container_dialog_frag)
        userFoundList_userName=rootView.findViewById(R.id.userName_user_found_layout)
        userFoundList_userEmail=rootView.findViewById(R.id.userEmail_user_found_layout)
        userFoundList_addFriendBtn=rootView.findViewById(R.id.add_friend_btn_user_found_layout)





        //binding Views ends here...

        userFoundList_addFriendBtn.setOnClickListener(){
            addFriendInUser(friendUserUID,friendUserEmail,friendUserName)
            this.dismiss()

        }

        rootView.bt_cancel_dialog_frag.setOnClickListener(){

            this.dismiss()


        }


   
    rootView.bt_add_dialog_frag.setOnClickListener(){

        hideInfoMessage()
        var dialog=UtillFunctions.setProgressDialog(requireContext(),"Searching Users..Please Wait!")
        UtillFunctions.showProgressDialog(dialog)

        var email= rootView.tv_email_dialog_frag.text.toString()
        if (!email.isEmpty()){
            db.getUser(email,object : FetchUsers{
                override fun OnAllUsersFetched(users: ArrayList<String>?) {
                    ;//nothing to do here for now
                }

                override fun OnSingleUserFetched(user: String?, userEmail: String?, userName: String?) {

                    if (user!=null){
                        friendUserUID=user
                        friendUserEmail=userEmail!!
                        friendUserName=userName!!
                        if (checkIfUserAlreadyFriend(user)){
//                            Toast.makeText(context, "$email : Already exist in your ledger list", Toast.LENGTH_SHORT).show()
                            UtillFunctions.hideProgressDialog(dialog)

                            return
                        }
                        showUserFoundList(user,userEmail,userName)
                        Toast.makeText(context, "$email : Found", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        showInfoMessage("Sorry! No user Found.")
                        Toast.makeText(context, "$email : No Such User Found", Toast.LENGTH_SHORT).show()

                    }
                    UtillFunctions.hideProgressDialog(dialog)
                }

            })

        }


        }




        return rootView

    }

    private fun checkIfUserAlreadyFriend(user: String): Boolean {
        var exist=false
        if (User.getUserSingleLedgers()!= null){
            if (!User.getUserSingleLedgers()!!.isEmpty())
            {

                for (i in User.getUserSingleLedgers()!!){
                    if (user!!.equals(i.friend_userID)){

                        exist=true
                    }

                }
        }
        }
        return exist




    }

    private fun addFriendInUser(friendUser: String, friendUserEmail: String, friendUserName: String) {
        Toast.makeText(context, "New Ledger Added", Toast.LENGTH_SHORT).show()
        UtillFunctions.createNewLedger(friendUser,friendUserEmail,friendUserName)
        isUserAdded=true
        adapter!!.notifyDataSetChanged()


    }

    private fun showUserFoundList(user: String, userEmail: String?, userName: String?) {

        userFoundList_userName.text=userName
        userFoundList_userEmail.text=userEmail
        userFoundList_Container.visibility=View.VISIBLE

    }

    private fun hideInfoMessage() {

        infoTextView.visibility=View.INVISIBLE;
    }

    private fun showInfoMessage(message:String) {

      infoTextView.text=message
infoTextView.visibility=View.VISIBLE;
    }}
