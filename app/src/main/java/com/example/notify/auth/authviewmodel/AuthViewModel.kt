package com.example.notify.auth.authviewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel: ViewModel(){
    private val auth = FirebaseAuth.getInstance()
    fun login(email: String, password: String,onResult:(Boolean) -> Unit)  {

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            onResult(it.isSuccessful)
        }

    }
    fun signup(email: String, password: String,onResult:(Boolean) -> Unit){
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            onResult(it.isSuccessful)
        }

    }
}