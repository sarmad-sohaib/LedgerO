package com.ledgero

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {

     private val TAG : String = "Sign UP Activity"
    private var auth = Firebase.auth

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private var userPhone: String = "+92"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)



        //sign Up OnClick Listener
        bt_signup_signUp.setOnClickListener(){

            //check if user's phone number and password is entered correctly
            if (userDetailCheck()){

                Log.i(TAG, "onCreate: User Details are correct, sending OTP...")
//         TODO: Remove Test Toast
             Toast.makeText(this,"Verifying Your Credentials",Toast.LENGTH_LONG).show()

                 val userPassword = tf_password_signUp.text.toString()

                var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks    = setCallBacksForPhoneVerification()

                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(userPhone)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)

            }



        }





        //already have an acoount, Login
    tv_login_signUp.setOnClickListener(){

        intent= Intent(this,LoginActivity::class.java)
        startActivity(intent)
    }
    }

    private fun setCallBacksForPhoneVerification(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {

       var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                 storedVerificationId = verificationId
                resendToken = token

                //Prompt user to enter verification code, sent by Firebase
                showdialog()
            }
        }
        return callbacks

    }

    private fun userDetailCheck(): Boolean {

        var uPhone: String= tf_user_phone_signUp.text.toString();
        var temp : String = ""
        if (tf_user_phone_signUp.text.isNullOrBlank() || tf_user_phone_signUp.length()!=11){
            
            Log.d(TAG, "onCreate: Phone Number Incorrect")
            Toast.makeText(this,"Phone Number Must Be 11 Digit Long",Toast.LENGTH_LONG).show()
            return false
        }
        if (uPhone[0]=='0'){
            Log.d(TAG, "userDetailCheck: At Index 0 it is  "+uPhone[0])

            Log.d(TAG, "userDetailCheck: Dropping digit 0 from the start of user number")

            for (i in 1 until uPhone.length){
               temp= temp+ uPhone[i].toString()
           }
            Log.d(TAG, "userDetailCheck: New Number is $temp")



        }
        userPhone= userPhone+temp;

        // TODO: Remove Test Toast
        Toast.makeText(this,"Usernumber : $userPhone",Toast.LENGTH_LONG).show()
        Log.d(TAG, "userDetailCheck: Usernumber : $userPhone")
        if (tf_password_signUp.text.isNullOrBlank() || tf_password_signUp.length()<6){

            Log.d(TAG, "onCreate: Password Incorrect")
            Toast.makeText(this,"Password must be of 6 or more characters",Toast.LENGTH_LONG).show()
            return false


        }
        //check if re-enter password field is filled or not
        if (!tf_reenter_password_signUp.text.isNullOrBlank()){
            
            //re-enter field is not empty..checking if it matches with password field
            if (!tf_reenter_password_signUp.text.toString().equals(tf_password_signUp.text.toString())){
                Log.d(TAG, "onCreate: Re-Enter Password does not match with Password")
                Toast.makeText(this,"Re-Enter Password does'nt match with Password",Toast.LENGTH_LONG).show()
                return false

            }
            
        }// prompt user that re-enter field is empty
        else{
            
            Log.d(TAG, "onCreate: Re-Enter Password is Empty")
            Toast.makeText(this,"Please Re-Enter the Password",Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }


    private fun showdialog(){
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)

        builder.setTitle("Enter Verification Code")

// Set up the input
        val input = EditText(this)
// Specify the number as type of input
        input.setHint("Enter Text")
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

// Set up the buttons
        builder.setPositiveButton("Verify", DialogInterface.OnClickListener { dialog, which ->

            // Check if user has entered the code correctly
            if (!input.text.isNullOrBlank()){

                //create credential object using PhoneAuthProvider so we can use this to Login User

                Log.d(TAG, "showdialog: Creating User Credentials...")
                val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, input.text.toString())
                Log.d(TAG, "showdialog: Calling SignInWithPhoneAuth")
                signInWithPhoneAuthCredential(credential)

            }
            Log.d(TAG, "showdialog: Null verification code submitted")

        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel()

            Log.d(TAG, "showdialog: User did'nt enterd verification code")
        })

        builder.show()
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    startActivity(Intent(this,MainActivity::class.java))

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Log.w(TAG, "signInWithCredential:Incorrect Verification Code", task.exception)

                        Toast.makeText(this,"Incorrect Verification Code",Toast.LENGTH_LONG).show()
                    }
                    // Update UI
                }
            }
    }
}