package com.example.notify.auth.authviewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel: ViewModel(){
    private val auth = FirebaseAuth.getInstance()
    fun login(){

    }
}