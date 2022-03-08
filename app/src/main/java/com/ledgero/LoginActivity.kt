package com.ledgero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

tv_signup_login.setOnClickListener(){

    intent = Intent(this, SignUpActivity::class.java)
    startActivity(intent)
}


        //onClick of Login Button
        bt_login_login.setOnClickListener(){

            if (!tf_phone_login.text.isNullOrBlank() && !tf_password_login.text.isNullOrBlank()){

// phone number and password is added


                Toast.makeText(this,"Nice!",Toast.LENGTH_LONG).show()

            }else{
                // phone number or password is missing
                Toast.makeText(this,"Please Enter Phone Number And Password",Toast.LENGTH_LONG).show()

            }
        }


    }




}