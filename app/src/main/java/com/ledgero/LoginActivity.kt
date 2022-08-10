package com.ledgero

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.ledgero.DataClasses.FriendUsers
import com.ledgero.DataClasses.User
import com.ledgero.Interfaces.FetchUsers
import com.ledgero.model.DatabaseUtill
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

   lateinit var loginViewModel : LoginViewModel

   val TAG:String ="LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginViewModel= ViewModelProvider(this).get(LoginViewModel::class.java)

        loginViewModel.setmyContext(this)
        if (loginViewModel.isUserLogin()){


            Log.d(TAG, "onCreate: User Already Login")

//            //call this function to update the current user data
//            //so whenever user login, its data will be fetched from
//            //firebase and will be updated.

                    DatabaseUtill().updateCurrentUser(User.userID!!,object :FetchUsers{
                        override fun OnAllUsersFetched(users: ArrayList<FriendUsers>?) {
//                            nothing to do it here
                        }

                        override fun OnSingleUserFetched(user: FriendUsers?) {

                           loginViewModel.context.startActivity(Intent(loginViewModel.context,MainActivity::class.java))
                            val ac= loginViewModel.context as Activity
                            ac.finish()

                        }

                    })





        }
        Log.d(TAG, "onCreate: User Not Logged In")

tv_signup_login.setOnClickListener(){


    intent = Intent(this, SignUpActivity::class.java)
    startActivity(intent)
}


        //onClick of Login Button
        bt_login_login.setOnClickListener(){

            loginViewModel.userEmail=tf_phone_login.text.toString()
            loginViewModel.userPassword= tf_password_login.text.toString()

            if (loginViewModel.validateUserInfo()){

                Log.d(TAG, "onCreate: User Input is Validated")

                Log.d(TAG, "onCreate: Passing User Info To Firebase and updating user")


                loginViewModel.signIn()
            }




            }


    }




}