package com.ledgero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_login.*

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

            loginViewModel.userEmail=tf_phone_login.text.toString()
            loginViewModel.userPassword= tf_password_login.text.toString()

            if (loginViewModel.validateUserInfo()){

                Log.d(TAG, "onCreate: User Input is Validated")

                Log.d(TAG, "onCreate: Passing User Info To Firebase")

                loginViewModel.signIn()
            }




            }


    }




}