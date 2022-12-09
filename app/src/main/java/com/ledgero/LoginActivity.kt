package com.ledgero

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.ledgero.DataClasses.User
import com.ledgero.Interfaces.OnUserDetailUpdate
import com.ledgero.ViewModels.LoginViewModel
import com.ledgero.model.DatabaseUtill
import com.ledgero.model.UtillFunctions
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    lateinit var loginViewModel: LoginViewModel

            private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var resendVerification: TextView

    val TAG: String = "LoginActivity"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        var dialog = UtillFunctions.setProgressDialog(this, "Checking Credentials...")



        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString $errorCode", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()

                    var fragmentName = intent.getStringExtra("fragmentName")
                    var ledgerUID = intent.getStringExtra("ledgerUID")
                    var intent = Intent(loginViewModel.context, MainActivity::class.java)
                    if (fragmentName != null) {
                        intent.putExtra("fragmentName", fragmentName)
                        intent.putExtra("ledgerUID", ledgerUID)
                        Log.d("TapBack", "onAuthenticationSucceeded: ledgerUID: $ledgerUID")

                    }
                    loginViewModel.context.startActivity(intent)

                    val ac = loginViewModel.context as Activity
                    ac.finish()

                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("LedgerO Biometric Log in")
            .setDescription("You are seeing this because you have already log in using your credentials")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        loginViewModel.setmyContext(this, this)



        if (loginViewModel.isUserLogin()) {


            Log.d(TAG, "onCreate: User Already Login")

//            //call this function to update the current user data
//            //so whenever user login, its data will be fetched from
//            //firebase and will be updated.

            DatabaseUtill().updateCurrentUser(User.userID!!, object : OnUserDetailUpdate {

                override fun onUserDetailsUpdated(boolean: Boolean) {

                    biometricPrompt.authenticate(promptInfo)


                }

            })


        } else {
            Log.d(TAG, "onCreate: User Not Logged In")
            Toast.makeText(this, "Please Login!", Toast.LENGTH_SHORT).show()
            UtillFunctions.hideProgressDialog(dialog)
        }


        resendVerification = findViewById<TextView>(R.id.resendVerification_login)
        resendVerification.setOnClickListener {
            sendVerificationCode()
        }



        tv_signup_login.setOnClickListener {


            intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }


        //onClick of Login Button
        bt_login_login.setOnClickListener {

            loginViewModel.userEmail = tf_phone_login.text.toString()
            loginViewModel.userPassword = tf_password_login.text.toString()

            if (loginViewModel.validateUserInfo()) {

                Log.d(TAG, "onCreate: User Input is Validated")

                Log.d(TAG, "onCreate: Passing User Info To Firebase and updating user")


                loginViewModel.signIn()
            }
        }


        tv_forgot_password_login.setOnClickListener {

            loginViewModel.userEmail=tf_phone_login.text.toString()
           if( loginViewModel.isValidEmail(loginViewModel.userEmail)) {
               try {

                   val dialog = UtillFunctions.setProgressDialog(this, " Sending Password Reset Link...")
                   UtillFunctions.showProgressDialog(dialog)
               FirebaseAuth.getInstance().sendPasswordResetEmail(loginViewModel.userEmail)
                   .addOnCompleteListener {
                       UtillFunctions.hideProgressDialog(dialog)
                       if (it.isSuccessful) {
                           Toast.makeText(
                               this,
                               "Password Reset Link Sent. Please check you email",
                               Toast.LENGTH_SHORT
                           ).show()

                       } else {
                           Toast.makeText(
                               this,
                               "Could not send reset link. ${it.exception?.message!!.toString()}",
                               Toast.LENGTH_SHORT
                           ).show()

                       }
                   }

           }catch (e:Exception){
                   UtillFunctions.hideProgressDialog(dialog)
                   Toast.makeText(
                       this,
                       "Something went wrong. Please try again later",
                       Toast.LENGTH_SHORT
                   ).show()
                   Log.d(TAG, "onCreate: ${e.localizedMessage}")
           }
           }else{
               Toast.makeText(this, "Please enter correct email address to send password reset link", Toast.LENGTH_SHORT).show()
           }
        }
    }


    fun sendVerificationCode() {
        val dialog = UtillFunctions.setProgressDialog(this, " Sending Verification Link...")
        UtillFunctions.showProgressDialog(dialog)
        loginViewModel.userEmail = tf_phone_login.text.toString()
        loginViewModel.userPassword = tf_password_login.text.toString()

        if (loginViewModel.validateUserInfo()) {
            try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    loginViewModel.userEmail,
                    loginViewModel.userPassword
                ).addOnCompleteListener { it ->
                    if (it.isSuccessful) {
                        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()!!
                            .addOnCompleteListener { link ->
                                if (link.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Verification Link Sent",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    resendVerification.visibility = View.GONE
                                    UtillFunctions.hideProgressDialog(dialog)

                                } else {
                                    UtillFunctions.hideProgressDialog(dialog)
                                    Toast.makeText(
                                        this,
                                        "Sorry! Not able to send verification link. Please try again later",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                            }
                    } else {
                        UtillFunctions.hideProgressDialog(dialog)
                        Toast.makeText(this, "Wrong Email or Password", Toast.LENGTH_SHORT).show()

                    }
                }
            } catch (e: Exception) {
                UtillFunctions.hideProgressDialog(dialog)
                Toast.makeText(this, "${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}