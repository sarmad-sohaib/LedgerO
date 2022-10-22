package com.ledgero

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
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

    val TAG: String = "LoginActivity"
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
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString $errorCode", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()

                    loginViewModel.context.startActivity(
                        Intent(
                            loginViewModel.context,
                            MainActivity::class.java
                        )
                    )
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

        loginViewModel.setmyContext(this)
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
    }
}