package com.ledgero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

   lateinit var loginViewViewModel : LoginViewModel

   val TAG:String ="LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginViewViewModel= ViewModelProvider(this).get(LoginViewModel::class.java)

        if (loginViewViewModel.checkLoginUser()){

            Log.d(TAG, "onCreate: User Already Login")
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return

        }
        Log.d(TAG, "onCreate: User Not Logged In")

tv_signup_login.setOnClickListener(){


    intent = Intent(this, SignUpActivity::class.java)
    startActivity(intent)
}


        //onClick of Login Button
        bt_login_login.setOnClickListener(){

            loginViewViewModel.userPhone=tf_phone_login.text.toString()
            loginViewViewModel.userPass=tf_password_login.text.toString()

            if (!loginViewViewModel.userPhone.isNullOrBlank() && !loginViewViewModel.userPass.isNullOrBlank()){

// phone number and password is added
                Log.d(TAG, "onCreate: Phone Number and Password is Added Correctly...Going To Login The User")




            }else{
                // phone number or password is missing
                Toast.makeText(this,"Please Enter Phone Number And Password",Toast.LENGTH_LONG).show()
                Log.d(TAG, "onCreate: Phone or Passwor is not added")
            }
        }


    }




}