package com.mikucode.mymezgebcloudposapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var googleSignInBtn: Button
    private lateinit var phoneSignInBtn: Button
    private var mAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        //configure and implement google Sign in
        googleSignInBtn = findViewById(R.id.signInGoogle)
        phoneSignInBtn = findViewById(R.id.signInPhone)
        phoneSignInBtn.setOnClickListener {
            Toast.makeText(this, "Coming Soon...",Toast.LENGTH_LONG).show()
        }
        googleSignInImplementation(googleSignInBtn)

    }


    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, getString(R.string.google_signup_failed),Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun googleSignInImplementation(googleSignInBtn: Button) {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInBtn .setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Hello + ${acct.displayName}"  ,Toast.LENGTH_SHORT).show()
                    val user = firebaseAuth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("userEmail",user?.email)
                    intent.putExtra("userName",user?.displayName)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.signin_failed)  ,Toast.LENGTH_SHORT).show() }
            }.addOnFailureListener {
                Toast.makeText(this, getString(R.string.login_failed) ,Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {

        // Check if user is signed in (non-null) and update UI accordingly.
        if(currentUser != null){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        super.onStart()
    }
}