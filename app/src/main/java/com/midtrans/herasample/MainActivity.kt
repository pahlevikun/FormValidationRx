package com.midtrans.herasample

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import com.jakewharton.rxbinding.widget.RxTextView
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initValidation()
    }

    private fun initValidation() {
        val compositeSubscription = CompositeSubscription()
        val emailObservable = RxTextView.textChanges(editTextEmail)
        val passwordObservable = RxTextView.textChanges(editTextPassword)

        val mEmailSubscription = emailObservable.doOnNext {
            emailEditTextError(1) // disable email error
        }
                .debounce(200, TimeUnit.MILLISECONDS)
                .filter { charSequence ->
                    !TextUtils.isEmpty(charSequence) // check if not null
                }.observeOn(AndroidSchedulers.mainThread()) // Main UI Thread
                .subscribe(object : Subscriber<CharSequence>() {
                    override fun onCompleted() {
                        // on Completed
                    }

                    override fun onError(e: Throwable) {
                        // Error
                        Log.e("mEmailSubscription", e.message)
                    }

                    override fun onNext(charSequence: CharSequence) {
                        // Check every user input for valid email address
                        if (!isUserInputValid(charSequence.toString(), 1)) {
                            emailEditTextError(2) // show error for invalid email
                        } else {
                            emailEditTextError(1) // hide error on valid email
                        }
                    }
                })
        compositeSubscription.add(mEmailSubscription) // Add email subscriber in composite subscription


        val mPasswordSubscription = passwordObservable
                .doOnNext { passwordEditTextError(1) }
                .debounce(200, TimeUnit.MILLISECONDS)
                .filter { charSequence -> !TextUtils.isEmpty(charSequence) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<CharSequence>() {
                    override fun onCompleted() {
                        // On Completed
                    }

                    override fun onError(e: Throwable) {
                        Log.e("mPasswordSubscription", e.message)
                    }

                    override fun onNext(charSequence: CharSequence) {
                        if (!isUserInputValid(charSequence.toString(), 2)) {
                            passwordEditTextError(2)
                        } else {
                            passwordEditTextError(1)
                        }
                    }
                })

        compositeSubscription.add(mPasswordSubscription)

        val allFieldsSubscription = Observable.combineLatest(emailObservable, passwordObservable) { mEmail, mPassword ->
            isUserInputValid(mEmail.toString(), 1) && isUserInputValid(mPassword.toString(), 2)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        Log.e("allFieldsSubscription", e.message)
                    }

                    override fun onNext(aBoolean: Boolean?) {
                        if (aBoolean!!) {
                            signInButtonState(1) // enable login button
                        } else {
                            signInButtonState(2) // disable login button
                        }
                    }
                })

        compositeSubscription.add(allFieldsSubscription)

    }


    private fun isUserInputValid(userInput: String, whichCase: Int): Boolean {
        when (whichCase) {
            1 // check email input
            -> return !TextUtils.isEmpty(userInput) && Patterns.EMAIL_ADDRESS.matcher(userInput).matches()
            2 // check password input
            -> return userInput.length >= 6
        }
        return false
    }

    private fun emailEditTextError(whichCase: Int) {
        when (whichCase) {
            1 // for hide error
            -> {
                if (textInputEmail.childCount == 2) {
                    textInputEmail.getChildAt(1).visibility = View.GONE
                }
                textInputEmail.error = null
            }
            2 // for show error
            -> {
                if (textInputEmail.childCount == 2) {
                    textInputEmail.getChildAt(1).visibility = View.VISIBLE
                }
                textInputEmail.error = "Error Email"
            }
        }
    }

    private fun passwordEditTextError(whichCase: Int) {
        when (whichCase) {
            1 // for hide error
            -> {
                if (textInputPassword.childCount == 2) {
                    textInputPassword.getChildAt(1).visibility = View.GONE
                }
                textInputPassword.error = null
            }
            2 // for show error
            -> {
                if (textInputPassword.childCount == 2) {
                    textInputPassword.getChildAt(1).visibility = View.VISIBLE
                }
                textInputPassword.error = "Empty Password"
            }
        }
    }

    private fun signInButtonState(whichCase: Int) {
        when (whichCase) {
            1 // enable button
            -> {
                buttonLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                buttonLogin.isEnabled = true
                buttonLogin.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            2 // disable button
            -> {
                buttonLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
                buttonLogin.isEnabled = false
                buttonLogin.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }

    }
}
