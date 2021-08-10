package com.app.letschat.ui.login

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Secure
import android.text.Html
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.app.letschat.R
import com.app.letschat.api.ApiHelperImpl
import com.app.letschat.api.RetrofitBuilder
import com.app.letschat.dialog.CustomLoaderDialog
import com.app.letschat.model.SigninRequest
import com.app.letschat.ui.forgotpassword.ForgotPasswordActivity
import com.app.letschat.ui.forgotpassword.HomeActivity
import com.app.letschat.ui.register.RegisterActivity
import com.app.letschat.utils.ViewModelFactory
import com.example.akaya.utils.AndroidUtility
import com.example.akaya.utils.Status


class LoginActivity:AppCompatActivity() {
    lateinit var tv_create:TextView
    lateinit var btn_signin:Button
    lateinit var tv_forgotpassword:TextView
    lateinit var et_email:EditText
    lateinit var et_password:EditText
    private lateinit var viewModel: Loginviewmodel
    lateinit var mCustomLoaderDialog: CustomLoaderDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        tv_create=findViewById(R.id.tv_create)
        val text = "<font color=#1D293F>New here?</font> <font color=#22B0FC><u>Create a new One</u></font>"
        tv_create.setText(Html.fromHtml(text))
        btn_signin=findViewById(R.id.btn_signin)
        tv_forgotpassword=findViewById(R.id.tv_forgotpassword)
        et_email=findViewById(R.id.et_email)
        et_password=findViewById(R.id.et_password)

        mCustomLoaderDialog = CustomLoaderDialog(this)

        setUpViewModel()

        setupObserver()


        btn_signin.setOnClickListener{
//          val intent=Intent(this,DobActivity::class.java)
//          startActivity(intent)
            callLoginApi()
        }
        tv_forgotpassword.setOnClickListener{
            val intent=Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        tv_create.setOnClickListener({
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        })

    }
    fun showLoader() {
        mCustomLoaderDialog.show()
    }

    fun hideLoader() {
        if (mCustomLoaderDialog.isShowing)
            mCustomLoaderDialog.cancel()
    }
    private fun setUpViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                ApiHelperImpl(RetrofitBuilder.apiService)
            )
        ).get(Loginviewmodel::class.java)
    }
    private fun setupObserver() {
        viewModel.signinData().observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    hideLoader()
                    val baseResponse = it.data
                    val errorCode = baseResponse?.status?.error_code2
                    when {
                        (errorCode == 0) -> {
                            AndroidUtility.showToast(this, "Login Successfully !!")
                                     val intent=Intent(this,HomeActivity::class.java)
                                       startActivity(intent)


                        }
                        (errorCode == 1) -> {
                            AndroidUtility.showToast(this, baseResponse.status?.message ?: "")
                        }
                    }


                }


                Status.LOADING -> {
                    showLoader()
                }
                Status.ERROR -> {
                    hideLoader()
                    AndroidUtility.showToast(this, getString(R.string.something_went_wrong))
                }
            }

        })
    }

    private fun callLoginApi(){
        val email=et_email.text.toString().trim()
        val password=et_password.text.toString().trim()


        when {
            (!AndroidUtility.isNetworkAvailable(this)) -> {
                AndroidUtility.showToast(this, getString(R.string.please_check_internet))
                return
            }
            email == "" -> {
                AndroidUtility.showToast(this, "Email can't be blank")
                return
            }
            (!AndroidUtility.isValidEmail(email)) -> {
                AndroidUtility.showToast(this, "Please enter a valid email.")
                return
            }

            password == "" -> {
                AndroidUtility.showToast(this, "Password can't be blank")
                return
            }
        }

        val signinRequest = SigninRequest().apply {
            val deviceId = Secure.getString(
                getContentResolver(),
                Secure.ANDROID_ID
            )

            this.source="MOB"
            this.device_token=deviceId
            this.device_type="1"
            this.email = email
            this.password = password
        }

        viewModel.requestsignin(this, signinRequest)




    }



}