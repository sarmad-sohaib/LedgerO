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
import com.ledgero.DataClasses.FriendUsers
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
 lateinit var friendUser: FriendUsers



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
            addFriendInUser(friendUser)
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
                override fun OnAllUsersFetched(users: ArrayList<FriendUsers>?) {
                    ;//nothing to do here for now
                }

                override fun OnSingleUserFetched(user: FriendUsers?) {

                    if (user!=null){
                        friendUser=user
                        if (checkIfUserAlreadyFriend(user)){
//                            Toast.makeText(context, "$email : Already exist in your ledger list", Toast.LENGTH_SHORT).show()
                            UtillFunctions.hideProgressDialog(dialog)

                            return
                        }
                        showUserFoundList(user)
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

    private fun checkIfUserAlreadyFriend(user: FriendUsers): Boolean {
        var exist=false
        if (User.user_single_Ledgers != null){
            if (!User.user_single_Ledgers!!.isEmpty())
            {

                for (i in User.user_single_Ledgers!!){
                    if (user.userID!!.equals(i.friend_user!!.userID)){

                        exist=true
                    }

                }
        }
        }
        return exist




    }

    private fun addFriendInUser(friendUser: FriendUsers) {
        Toast.makeText(context, "New Ledger Added", Toast.LENGTH_SHORT).show()
        User.add_friend_for_single_ledger(friendUser)
        isUserAdded=true
        adapter!!.notifyDataSetChanged()


    }

    private fun showUserFoundList(user: FriendUsers) {

        userFoundList_userName.text=user.userName
        userFoundList_userEmail.text=user.userEmail
        userFoundList_Container.visibility=View.VISIBLE

    }

    private fun hideInfoMessage() {

        infoTextView.visibility=View.INVISIBLE;
    }

    private fun showInfoMessage(message:String) {

      infoTextView.text=message
infoTextView.visibility=View.VISIBLE;
    }}
