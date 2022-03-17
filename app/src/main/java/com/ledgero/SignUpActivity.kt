package com.ledgero

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {

    private val TAG: String = "Sign UP Activity"

    lateinit var signUpViewModel: SignUpViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        signUpViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        signUpViewModel.initializeFields(
            tf_user_phone_signUp,
            tf_password_signUp,
            tf_reenter_password_signUp,
            this
        )
        //sign Up OnClick Listener
        bt_signup_signUp.setOnClickListener() {


            //PhoneAuth Start -----------
            signUpViewModel.initializeFields(tf_user_phone_signUp, tf_password_signUp, tf_reenter_password_signUp, this)
            //check if user's phone number and password is entered correctly
            if (signUpViewModel.userDetailCheck()) {


                //checking if user exist or not
//                if (signUpViewModel.isUserExist(signUpViewModel.userPhone)) {
//
//                    Toast.makeText(this, "User Already Exist", Toast.LENGTH_LONG).show()
//                    return@setOnClickListener
//
//                }


                Log.i(TAG, "onCreate: New User Needs to registerd")
                Log.d(TAG, "onCreate: User Details are correct, sending OTP...")
//         TODO: Remove Test Toast
                Toast.makeText(this, "Verifying Your Credentials", Toast.LENGTH_LONG).show()


                var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
                    signUpViewModel.setCallBacksForPhoneVerification()

                val options = PhoneAuthOptions.newBuilder(signUpViewModel.auth)
                    .setPhoneNumber(signUpViewModel.userPhone) // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)

            }
        //Phone Auth Ends Here ******--------**********-------****------

        }


        //already have an acoount, Login
        tv_login_signUp.setOnClickListener() {

            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }


}
