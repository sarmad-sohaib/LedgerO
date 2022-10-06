package com.ledgero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.ledgero.ViewModels.SignUpViewModel
import kotlinx.android.synthetic.main.activity_sign_up.*

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
        bt_signup_signUp.setOnClickListener {
            signUpViewModel.initializeFields(tf_user_phone_signUp, tf_password_signUp, tf_reenter_password_signUp, this)


//            Email Auth Start
           signUpViewModel.signUpWithemail()

           }

//          Email Auth Ends Here






//            //PhoneAuth Start -----------
//            //check if user's phone number and password is entered correctly
//            if (signUpViewModel.userDetailCheck()) {
//
//
//                //checking if user exist or not
////                if (signUpViewModel.isUserExist(signUpViewModel.userPhone)) {
////
////                    Toast.makeText(this, "User Already Exist", Toast.LENGTH_LONG).show()
////                    return@setOnClickListener
////
////                }
//
//
//                Log.i(TAG, "onCreate: New User Needs to registerd")
//                Log.d(TAG, "onCreate: User Details are correct, sending OTP...")
////         TODO: Remove Test Toast
//                Toast.makeText(this, "Verifying Your Credentials", Toast.LENGTH_LONG).show()
//
//
//                var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
//                    signUpViewModel.setCallBacksForPhoneVerification()
//
//                val options = PhoneAuthOptions.newBuilder(signUpViewModel.auth)
//                    .setPhoneNumber(signUpViewModel.userPhone) // Phone number to verify
//                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                    .setActivity(this)                 // Activity (for callback binding)
//                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
//                    .build()
//                PhoneAuthProvider.verifyPhoneNumber(options)
//
//            }
//        //Phone Auth Ends Here ******--------**********-------****------
//


   //     }


        //already have an acoount, Login
        tv_login_signUp.setOnClickListener {

            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }


}
