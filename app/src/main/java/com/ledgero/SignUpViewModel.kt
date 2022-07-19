package com.ledgero

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.SingleLedgers
import com.ledgero.DataClasses.User
import java.util.*
import kotlin.collections.ArrayList

class SignUpViewModel: ViewModel() {
    //user details
    //TODO: Use ViewModelFactory to Initialize these fields
    lateinit var tf_user_phone_signUp: TextInputEditText
    lateinit var tf_password_signUp: TextInputEditText
    lateinit var tf_reenter_password_signUp: TextInputEditText
    lateinit var tf_user_email_signUp: TextInputEditText
    lateinit var context: Context
    var auth = Firebase.auth
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    var userPhone: String = "+92"
    val TAG = "SignUP ViewModel"
    lateinit var mUser: FirebaseUser
    private var isUserCreated = false





    fun signUpWithemail(){

        if (!isValidEmail(tf_user_email_signUp.text.toString())){
            Log.d(TAG, "signUpWithemail: Email Not Valid")
            Toast.makeText(context,"Please Enter Valid Email Address",Toast.LENGTH_LONG).show()
            return
        }

        if (!isValidPassword()){
            Log.d(TAG, "signUpWithemail: Invalid Password")
            Toast.makeText(context, "Please Enter Password Again!!", Toast.LENGTH_SHORT).show()

            return
        }

        Log.d(TAG, "signUpWithemail: Email And Password is correct, Going To Sign Up")

        createUserAccount(tf_user_email_signUp.text.toString(),tf_password_signUp.text.toString())

    }

    private fun createUserAccount(email: String, password: String): Boolean {



        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserAccount:success")
                   mUser = auth.currentUser!!

                    Toast.makeText(context, "Account Created Successfully!, Saving User in DB", Toast.LENGTH_SHORT).show()


                    saveuserinDB(mUser)

                    context.startActivity(Intent(context,MainActivity::class.java))
                    val ac = context as Activity
                    ac.finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(context, "User Already Exists.",
                        Toast.LENGTH_SHORT).show()
                   isUserCreated=false
                }
            }
        return isUserCreated

    }

    //validate Email Addrress
   private fun isValidEmail(email:String):Boolean{

        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

   private fun isValidPassword():Boolean{

        if (tf_password_signUp.text.isNullOrBlank() || tf_password_signUp.length() < 6) {

            Log.d(TAG, "onCreate: Password Incorrect")
            Toast.makeText(
                context,
                "Password must be of 6 or more characters",
                Toast.LENGTH_LONG
            )
                .show()
            return false


        }
        //check if re-enter password field is filled or not
        if (!tf_reenter_password_signUp.text.isNullOrBlank()) {

            //re-enter field is not empty..checking if it matches with password field
            if (!tf_reenter_password_signUp.text.toString()
                    .equals(tf_password_signUp.text.toString())
            ) {
                Log.d(TAG, "onCreate: Re-Enter Password does not match with Password")
                Toast.makeText(
                    context,
                    "Re-Enter Password does'nt match with Password",
                    Toast.LENGTH_LONG
                ).show()
                return false

            }

        }// prompt user that re-enter field is empty
        else {

            Log.d(TAG, "onCreate: Re-Enter Password is Empty")
            Toast.makeText(context, "Please Re-Enter the Password", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }














    //---- Phone Auth Start ------//
    //*****************************************************************************************************

        fun initializeFields(
            phone: TextInputEditText,
            password: TextInputEditText,
            reenterPass: TextInputEditText,
            dcontext: Context,

        ) {
            this.tf_user_phone_signUp = phone
            this.tf_password_signUp = password
            this.tf_reenter_password_signUp = reenterPass
            this.context = dcontext
            this.tf_user_email_signUp=phone

        }

        fun userDetailCheck(): Boolean {

            var uPhone: String = tf_user_phone_signUp.text.toString();
            var temp: String = ""
            if (tf_user_phone_signUp.text.isNullOrBlank() || tf_user_phone_signUp.length() != 11) {

                Log.d(TAG, "onCreate: Phone Number Incorrect")
                Toast.makeText(context, "Phone Number Must Be 11 Digit Long", Toast.LENGTH_LONG)
                    .show()
                return false
            }
            if (uPhone[0] == '0') {
                Log.d(TAG, "userDetailCheck: At Index 0 it is  " + uPhone[0])

                Log.d(TAG, "userDetailCheck: Dropping digit 0 from the start of user number")

                for (i in 1 until uPhone.length) {
                    temp = temp + uPhone[i].toString()
                }
                Log.d(TAG, "userDetailCheck: New Number is $temp")


            }
            userPhone = "+92" + temp;

            // TODO: Remove Test Toast
            Toast.makeText(context, "Usernumber : $userPhone", Toast.LENGTH_LONG).show()
            Log.d(TAG, "userDetailCheck: Usernumber : $userPhone")

            if (!isValidPassword()){
                return false
            }

            return true
        }

        fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")


                        val user = task.result?.user

                        context.startActivity(Intent(context, MainActivity::class.java))

                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            Log.w(
                                TAG,
                                "signInWithCredential:Incorrect Verification Code",
                                task.exception
                            )

                            Toast.makeText(
                                context,
                                "Incorrect Verification Code",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                        // Update UI
                        Toast.makeText(context, "Sign in Failed", Toast.LENGTH_LONG).show()
                    }
                }
        }

        fun saveuserinDB(user: FirebaseUser?) {



//            here User is created. User is Singleton class
            User.userEmail=tf_user_email_signUp.text.toString()
            User.userID= user?.uid.toString()
            User.total_single_ledgers=0;
            User.userName="New User"
            User.userPhone="00000"
            User.user_group_Ledgers=null
            User.user_single_Ledgers=null
            User.user_total_give=0
            User.user_total_take=0


            Toast.makeText(context, "User Created...Uploading", Toast.LENGTH_SHORT).show()

            var db= FirebaseDatabase.getInstance().reference

            db.child("/users").child(User.userID!!).setValue(User)

        }

        fun isUserExist(phone: String): Boolean {

            Log.d(TAG, "isUserExist: called")
            val db = FirebaseDatabase.getInstance().getReference()
            // db_reference.child()
            Log.d(TAG, "isUserExist: ${db.child("/users").get().result.toString()}")
            var result = db.child("users").get().result
            Log.d(TAG, "isUserExist: result fetched")
            if (result.value != null) {
                Log.d(TAG, "isUserExist: Yes, User Exists")
                var user: User = result.value as User
                Toast.makeText(context, "User Already Exist : ${user.userID}", Toast.LENGTH_LONG)
                    .show()

                return true
            } else {
                Log.d(TAG, "isUserExist: No, User Found")

                return false

            }


        }

        fun setCallBacksForPhoneVerification(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {

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

        fun showdialog() {
            val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)

            builder.setTitle("Enter Verification Code")

// Set up the input
            val input = EditText(context)
// Specify the number as type of input
            input.setHint("Enter Text")
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)

// Set up the buttons
            builder.setPositiveButton("Verify", DialogInterface.OnClickListener { dialog, which ->

                // Check if user has entered the code correctly
                if (!input.text.isNullOrBlank()) {

                    //create credential object using PhoneAuthProvider so we can use this to Login User

                    Log.d("Sign UP Activity", "showdialog: Creating User Credentials...")
                    val credential = PhoneAuthProvider.getCredential(
                        storedVerificationId!!,
                        input.text.toString()
                    )
                    Log.d("Sign UP Activity", "showdialog: Calling SignInWithPhoneAuth")
                    signInWithPhoneAuthCredential(credential)

                }
                Log.d("Sign UP Activity", "showdialog: Null verification code submitted")

            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()

                Log.d("Sign UP Activity", "showdialog: User did'nt enterd verification code")
            })

            builder.show()
        }

    //*****************************************************************************************************
//---- Phone Auth Ends ------//




}

